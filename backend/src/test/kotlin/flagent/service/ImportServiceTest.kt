package flagent.service

import flagent.domain.entity.Flag
import flagent.domain.repository.IFlagRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportServiceTest {

    @Test
    fun importFromContent_unsupportedFormat_returnsError() = runBlocking {
        val flagService = mockk<FlagService>(relaxed = true)
        val segmentService = mockk<SegmentService>(relaxed = true)
        val variantService = mockk<VariantService>(relaxed = true)
        val distributionService = mockk<DistributionService>(relaxed = true)
        val constraintService = mockk<ConstraintService>(relaxed = true)
        val flagRepository = mockk<IFlagRepository>(relaxed = true)
        val service = ImportService(
            flagService, segmentService, variantService,
            distributionService, constraintService, flagRepository
        )
        val result = service.importFromContent("xml", "<root/>", null)
        assertEquals(0, result.created)
        assertEquals(0, result.updated)
        assertTrue(result.errors.size == 1 && result.errors[0].startsWith("Unsupported format"))
    }

    @Test
    fun importFromContent_invalidJson_returnsParseError() = runBlocking {
        val flagService = mockk<FlagService>(relaxed = true)
        val segmentService = mockk<SegmentService>(relaxed = true)
        val variantService = mockk<VariantService>(relaxed = true)
        val distributionService = mockk<DistributionService>(relaxed = true)
        val constraintService = mockk<ConstraintService>(relaxed = true)
        val flagRepository = mockk<IFlagRepository>(relaxed = true)
        val service = ImportService(
            flagService, segmentService, variantService,
            distributionService, constraintService, flagRepository
        )
        val result = service.importFromContent("json", "{ invalid }", null)
        assertEquals(0, result.created)
        assertEquals(0, result.updated)
        assertTrue(result.errors.size == 1 && result.errors[0].startsWith("Parse error"))
    }

    @Test
    fun importFromContent_validJson_emptyFlags_returnsZeroCreated() = runBlocking {
        val flagService = mockk<FlagService>(relaxed = true)
        val segmentService = mockk<SegmentService>(relaxed = true)
        val variantService = mockk<VariantService>(relaxed = true)
        val distributionService = mockk<DistributionService>(relaxed = true)
        val constraintService = mockk<ConstraintService>(relaxed = true)
        val flagRepository = mockk<IFlagRepository>(relaxed = true)
        val service = ImportService(
            flagService, segmentService, variantService,
            distributionService, constraintService, flagRepository
        )
        val result = service.importFromContent("json", """{"flags":[]}""", null)
        assertEquals(0, result.created)
        assertEquals(0, result.updated)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun importFromContent_validJson_newFlag_createsFlag() = runBlocking {
        val flagService = mockk<FlagService>(relaxed = true)
        val segmentService = mockk<SegmentService>(relaxed = true)
        val variantService = mockk<VariantService>(relaxed = true)
        val distributionService = mockk<DistributionService>(relaxed = true)
        val constraintService = mockk<ConstraintService>(relaxed = true)
        val flagRepository = mockk<IFlagRepository>(relaxed = true)
        coEvery { flagRepository.findByKey("new_flag") } returns null
        coEvery { flagService.createFlag(any(), any()) } returns Flag(id = 1, key = "new_flag", description = "Desc")
        coEvery { flagService.updateFlag(any(), any(), any()) } returns Flag(id = 1, key = "new_flag", description = "Desc")
        coEvery { flagService.setFlagEnabled(any(), any(), any()) } returns null
        val service = ImportService(
            flagService, segmentService, variantService,
            distributionService, constraintService, flagRepository
        )
        val json = """{"flags":[{"key":"new_flag","description":"Desc","segments":[],"variants":[]}]}"""
        val result = service.importFromContent("json", json, "importer")
        assertEquals(1, result.created)
        assertEquals(0, result.updated)
    }

    @Test
    fun importFromContent_validYaml_emptyFlags_returnsZeroCreated() = runBlocking {
        val flagService = mockk<FlagService>(relaxed = true)
        val segmentService = mockk<SegmentService>(relaxed = true)
        val variantService = mockk<VariantService>(relaxed = true)
        val distributionService = mockk<DistributionService>(relaxed = true)
        val constraintService = mockk<ConstraintService>(relaxed = true)
        val flagRepository = mockk<IFlagRepository>(relaxed = true)
        val service = ImportService(
            flagService, segmentService, variantService,
            distributionService, constraintService, flagRepository
        )
        val yaml = "version: \"1\"\nflags: []"
        val result = service.importFromContent("yaml", yaml, null)
        assertEquals(0, result.created)
        assertEquals(0, result.updated)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun importFromContent_validJson_existingFlag_updatesFlag() = runBlocking {
        val flagService = mockk<FlagService>(relaxed = true)
        val segmentService = mockk<SegmentService>(relaxed = true)
        val variantService = mockk<VariantService>(relaxed = true)
        val distributionService = mockk<DistributionService>(relaxed = true)
        val constraintService = mockk<ConstraintService>(relaxed = true)
        val flagRepository = mockk<IFlagRepository>(relaxed = true)
        val existing = Flag(id = 1, key = "existing", description = "Old")
        coEvery { flagRepository.findByKey("existing") } returns existing
        coEvery { flagRepository.findById(1) } returns existing
        coEvery { flagService.updateFlag(any(), any(), any()) } returns existing.copy(description = "New")
        coEvery { flagService.setFlagEnabled(any(), any(), any()) } returns null
        coEvery { segmentService.findSegmentsByFlagId(1) } returns emptyList()
        val service = ImportService(
            flagService, segmentService, variantService,
            distributionService, constraintService, flagRepository
        )
        val json = """{"flags":[{"key":"existing","description":"New","segments":[],"variants":[]}]}"""
        val result = service.importFromContent("json", json, "importer")
        assertEquals(0, result.created)
        assertEquals(1, result.updated)
    }
}
