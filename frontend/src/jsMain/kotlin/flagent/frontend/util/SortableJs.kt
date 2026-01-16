package flagent.frontend.util

import kotlinx.browser.window
import org.w3c.dom.Element

/**
 * JS interop wrapper for SortableJS library (loaded via CDN)
 */
external interface SortableOptions {
    var animation: Int?
    var handle: String?
    var ghostClass: String?
    var chosenClass: String?
    var dragClass: String?
    var onStart: ((event: SortableEvent) -> Unit)?
    var onEnd: ((event: SortableEvent) -> Unit)?
    var onUpdate: ((event: SortableEvent) -> Unit)?
    var onMove: ((event: SortableMoveEvent) -> Boolean)?
}

external interface SortableEvent {
    val item: Element
    val oldIndex: Int?
    val newIndex: Int?
    val from: Element
    val to: Element
}

external interface SortableMoveEvent {
    val dragged: Element
    val related: Element
    val willInsertAfter: Boolean
}

external interface SortableInstance {
    fun toArray(): Array<String>
    fun sort(order: Array<String>)
    fun destroy()
}

/**
 * Create Sortable instance with Kotlin-friendly API
 * SortableJS is loaded via CDN and available as window.Sortable
 */
fun createSortable(
    element: Element?,
    onUpdate: ((oldIndex: Int, newIndex: Int) -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onEnd: (() -> Unit)? = null,
    handle: String? = null,
    ghostClass: String = "sortable-ghost",
    chosenClass: String = "sortable-chosen",
    dragClass: String = "sortable-drag"
): SortableInstance? {
    if (element == null) return null
    
    val options = js("{}").unsafeCast<SortableOptions>()
    options.animation = 150
    options.handle = handle
    options.ghostClass = ghostClass
    options.chosenClass = chosenClass
    options.dragClass = dragClass
    
    options.onStart = { event ->
        onStart?.invoke()
    }
    
    options.onEnd = { event ->
        onEnd?.invoke()
    }
    
    options.onUpdate = { event ->
        val oldIndex = event.oldIndex
        val newIndex = event.newIndex
        if (oldIndex != null && newIndex != null) {
            onUpdate?.invoke(oldIndex, newIndex)
        }
    }
    
    return try {
        val SortableConstructor = window.asDynamic().Sortable
        if (SortableConstructor != null) {
            val instance = js("new window.Sortable(element, options)")
            instance.unsafeCast<SortableInstance>()
        } else {
            console.warn("SortableJS not loaded")
            null
        }
    } catch (e: Throwable) {
        console.error("Failed to create Sortable instance", e)
        null
    }
}
