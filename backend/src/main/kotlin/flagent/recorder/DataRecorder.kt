package flagent.recorder

import flagent.service.EvalResult

/**
 * DataRecorder interface for recording evaluation results
 * Maps to pkg/handler/data_recorder.go from original project
 */
interface DataRecorder {
    /**
     * Asynchronously record evaluation result
     */
    suspend fun record(result: EvalResult)
    
    /**
     * Record batch of evaluation results
     */
    suspend fun recordBatch(results: List<EvalResult>)
    
    /**
     * Create data record frame from evaluation result
     */
    fun newDataRecordFrame(result: EvalResult): DataRecordFrame
}
