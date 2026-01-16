package flagent.recorder.impl

import flagent.recorder.DataRecorder
import flagent.recorder.DataRecordFrame
import flagent.recorder.DataRecordFrameOptions
import flagent.service.EvalResult

/**
 * NoopRecorder - no-op implementation for disabling data recording
 */
class NoopRecorder : DataRecorder {
    override suspend fun record(result: EvalResult) {
        // No-op
    }
    
    override suspend fun recordBatch(results: List<EvalResult>) {
        // No-op
    }
    
    override fun newDataRecordFrame(result: EvalResult): DataRecordFrame {
        return DataRecordFrame(
            evalResult = result,
            options = DataRecordFrameOptions()
        )
    }
}
