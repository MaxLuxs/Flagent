package flagent.service.import

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Import format for GitOps - matches docs/private/01-backend/gitops.md schema.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FlagsImportFile(
    val version: String? = "1",
    val flags: List<FlagImportItem> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FlagImportItem(
    val key: String,
    val description: String = "",
    val enabled: Boolean = false,
    val notes: String? = null,
    val dataRecordsEnabled: Boolean = false,
    val entityType: String? = null,
    val segments: List<SegmentImportItem> = emptyList(),
    val variants: List<VariantImportItem> = emptyList(),
    val tags: List<String> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SegmentImportItem(
    val rank: Int = 999,
    val description: String? = null,
    @JsonProperty("rolloutPercent") val rolloutPercent: Int = 100,
    val constraints: List<ConstraintImportItem> = emptyList(),
    val distributions: List<DistributionImportItem> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConstraintImportItem(
    val property: String,
    val operator: String,
    val value: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DistributionImportItem(
    @JsonProperty("variantKey") val variantKey: String,
    val percent: Int = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VariantImportItem(
    val key: String,
    val attachment: Map<String, String>? = null
)
