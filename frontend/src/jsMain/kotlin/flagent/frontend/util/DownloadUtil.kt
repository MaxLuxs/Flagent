package flagent.frontend.util

import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Triggers browser download of bytes as a file with auth-compatible flow.
 */
fun triggerDownload(bytes: ByteArray, filename: String, mimeType: String) {
    val blob = js("new Blob([bytes], {type: mimeType})")
    val url = js("URL.createObjectURL(blob)").toString()
    val a = document.createElement("a") as org.w3c.dom.HTMLAnchorElement
    a.href = url
    a.download = filename
    document.body?.appendChild(a)
    a.click()
    document.body?.removeChild(a)
    js("URL.revokeObjectURL(url)")
}
