package flagent.service

import flagent.domain.entity.CrashReport
import flagent.repository.impl.CrashReportRepository

/**
 * OSS crash report service. tenantId is always null for OSS.
 */
class CrashReportService(
    private val crashReportRepository: CrashReportRepository
) {
    suspend fun save(crash: CrashReport): CrashReport = crashReportRepository.save(crash)

    suspend fun saveBatch(crashes: List<CrashReport>): List<CrashReport> =
        crashReportRepository.saveBatch(crashes)

    suspend fun list(
        tenantId: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int = 50,
        offset: Int = 0
    ): List<CrashReport> =
        crashReportRepository.list(tenantId, startTime, endTime, limit, offset)

    suspend fun count(tenantId: String?, startTime: Long?, endTime: Long?): Long =
        crashReportRepository.count(tenantId, startTime, endTime)
}
