package flagent.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import flagent.domain.repository.IFlagRepository
import flagent.service.command.CreateConstraintCommand
import flagent.service.command.CreateFlagCommand
import flagent.service.command.CreateSegmentCommand
import flagent.service.command.CreateVariantCommand
import flagent.service.command.DistributionItemCommand
import flagent.service.command.PutDistributionsCommand
import flagent.service.command.PutFlagCommand
import flagent.service.import.FlagImportItem
import flagent.service.import.FlagsImportFile
import kotlinx.serialization.Serializable
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * ImportService - imports flags from YAML/JSON (GitOps).
 */
class ImportService(
    private val flagService: FlagService,
    private val segmentService: SegmentService,
    private val variantService: VariantService,
    private val distributionService: DistributionService,
    private val constraintService: ConstraintService,
    private val flagRepository: IFlagRepository
) {
    private val jsonMapper =
        ObjectMapper().registerModule(com.fasterxml.jackson.module.kotlin.kotlinModule())
    private val yamlMapper =
        ObjectMapper(YAMLFactory()).registerModule(com.fasterxml.jackson.module.kotlin.kotlinModule())

    @Serializable
    data class ImportResult(
        val created: Int = 0,
        val updated: Int = 0,
        val errors: List<String> = emptyList()
    )

    suspend fun importFromContent(
        format: String,
        content: String,
        updatedBy: String? = null
    ): ImportResult {
        val flags = try {
            when (format.lowercase()) {
                "yaml", "yml" -> yamlMapper.readValue<FlagsImportFile>(content).flags
                "json" -> jsonMapper.readValue<FlagsImportFile>(content).flags
                else -> return ImportResult(errors = listOf("Unsupported format: $format. Use yaml or json."))
            }
        } catch (e: Exception) {
            logger.warn(e) { "Import parse error: ${e.message}" }
            return ImportResult(errors = listOf("Parse error: ${e.message}"))
        }
        var created = 0
        var updated = 0
        val errors = mutableListOf<String>()
        for (flagItem in flags) {
            try {
                val existing = flagRepository.findByKey(flagItem.key)
                if (existing != null) {
                    updateFlag(existing.id, flagItem, updatedBy)
                    updated++
                } else {
                    createFlag(flagItem, updatedBy)
                    created++
                }
            } catch (e: Exception) {
                logger.warn(e) { "Import flag ${flagItem.key} failed: ${e.message}" }
                errors.add("${flagItem.key}: ${e.message}")
            }
        }
        return ImportResult(created = created, updated = updated, errors = errors)
    }

    private suspend fun createFlag(flagItem: FlagImportItem, updatedBy: String?) {
        val created = flagService.createFlag(
            CreateFlagCommand(key = flagItem.key, description = flagItem.description),
            updatedBy
        )
        flagService.updateFlag(
            created.id,
            PutFlagCommand(
                description = flagItem.description,
                key = flagItem.key,
                dataRecordsEnabled = flagItem.dataRecordsEnabled,
                entityType = flagItem.entityType,
                notes = flagItem.notes
            ),
            updatedBy
        )
        if (flagItem.enabled) {
            flagService.setFlagEnabled(created.id, true, updatedBy)
        }
        val variantKeys =
            flagItem.segments.flatMap { it.distributions.map { d -> d.variantKey } }.distinct()
        val explicitVariants = flagItem.variants.map { it.key }
        val allVariantKeys = (variantKeys + explicitVariants).distinct()
        val variantMap = mutableMapOf<String, Int>()
        for (vk in allVariantKeys) {
            val v = variantService.createVariant(
                CreateVariantCommand(
                    flagId = created.id,
                    key = vk,
                    attachment = flagItem.variants.find { it.key == vk }?.attachment
                ),
                updatedBy
            )
            variantMap[vk] = v.id
        }
        for (segItem in flagItem.segments) {
            val seg = segmentService.createSegment(
                CreateSegmentCommand(
                    flagId = created.id,
                    description = segItem.description ?: "",
                    rolloutPercent = segItem.rolloutPercent
                ),
                updatedBy
            )
            val distItems = segItem.distributions.mapNotNull { d ->
                variantMap[d.variantKey]?.let {
                    DistributionItemCommand(
                        variantID = it,
                        variantKey = d.variantKey,
                        percent = d.percent
                    )
                }
            }
            if (distItems.isNotEmpty() && distItems.sumOf { it.percent } == 100) {
                distributionService.updateDistributions(
                    PutDistributionsCommand(
                        flagId = created.id,
                        segmentId = seg.id,
                        distributions = distItems
                    ),
                    updatedBy
                )
            }
            for (c in segItem.constraints) {
                constraintService.createConstraint(
                    CreateConstraintCommand(
                        segmentId = seg.id,
                        property = c.property,
                        operator = c.operator,
                        value = c.value
                    ),
                    updatedBy
                )
            }
        }
    }

    private suspend fun updateFlag(flagId: Int, flagItem: FlagImportItem, updatedBy: String?) {
        flagService.updateFlag(
            flagId,
            PutFlagCommand(
                description = flagItem.description,
                key = flagItem.key,
                dataRecordsEnabled = flagItem.dataRecordsEnabled,
                entityType = flagItem.entityType,
                notes = flagItem.notes
            ),
            updatedBy
        )
        flagService.setFlagEnabled(flagId, flagItem.enabled, updatedBy)
        val flag = flagRepository.findById(flagId) ?: return
        val existingSegments = segmentService.findSegmentsByFlagId(flagId)
        for (seg in existingSegments) {
            segmentService.deleteSegment(seg.id, updatedBy)
        }
        val variantKeys =
            flagItem.segments.flatMap { it.distributions.map { d -> d.variantKey } }.distinct()
        val explicitVariants = flagItem.variants.map { it.key }
        val allVariantKeys = (variantKeys + explicitVariants).distinct()
        val variantMap = flag.variants.associateBy { it.key }.toMutableMap()
        for (vk in allVariantKeys) {
            if (vk !in variantMap) {
                val v = variantService.createVariant(
                    CreateVariantCommand(
                        flagId = flagId,
                        key = vk,
                        attachment = flagItem.variants.find { it.key == vk }?.attachment
                    ),
                    updatedBy
                )
                variantMap[vk] = v
            }
        }
        for (segItem in flagItem.segments) {
            val seg = segmentService.createSegment(
                CreateSegmentCommand(
                    flagId = flagId,
                    description = segItem.description ?: "",
                    rolloutPercent = segItem.rolloutPercent
                ),
                updatedBy
            )
            val distItems = segItem.distributions.mapNotNull { d ->
                variantMap[d.variantKey]?.let {
                    DistributionItemCommand(
                        variantID = it.id,
                        variantKey = d.variantKey,
                        percent = d.percent
                    )
                }
            }
            if (distItems.isNotEmpty() && distItems.sumOf { it.percent } == 100) {
                distributionService.updateDistributions(
                    PutDistributionsCommand(
                        flagId = flagId,
                        segmentId = seg.id,
                        distributions = distItems
                    ),
                    updatedBy
                )
            }
            for (c in segItem.constraints) {
                constraintService.createConstraint(
                    CreateConstraintCommand(
                        segmentId = seg.id,
                        property = c.property,
                        operator = c.operator,
                        value = c.value
                    ),
                    updatedBy
                )
            }
        }
    }
}
