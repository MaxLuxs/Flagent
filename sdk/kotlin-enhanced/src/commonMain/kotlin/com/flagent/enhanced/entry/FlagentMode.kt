package com.flagent.enhanced.entry

/**
 * Client mode: server-side evaluation (HTTP per request) or offline/client-side (local snapshot).
 */
enum class FlagentMode {
    /** Evaluate via Evaluation API (POST /evaluation). */
    SERVER,
    /** Client-side evaluation using exported snapshot (ExportApi + FlagApi, local evaluator). */
    OFFLINE
}
