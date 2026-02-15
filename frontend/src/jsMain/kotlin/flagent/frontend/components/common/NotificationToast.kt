package flagent.frontend.components.common

import androidx.compose.runtime.*
import flagent.frontend.components.Icon
import flagent.frontend.state.Notification
import flagent.frontend.state.NotificationType
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.borderLeft
import flagent.frontend.util.flexShrink
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun NotificationToast(
    notifications: List<Notification>,
    onDismiss: (String) -> Unit
) {
    Div({
        style {
            position(Position.Fixed)
            property("top", "80px")
            property("right", "20px")
            property("z-index", "9999")
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(12.px)
            maxWidth(400.px)
            
            // Mobile responsive
            property("@media (max-width: 768px)", """
                {
                    right: 10px;
                    left: 10px;
                    max-width: calc(100% - 20px);
                }
            """.trimIndent())
        }
    }) {
        notifications.forEach { notification ->
            NotificationItem(notification, onDismiss)
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onDismiss: (String) -> Unit
) {
    val themeMode = LocalThemeMode.current
    // Auto-dismiss after duration
    LaunchedEffect(notification.id) {
        window.setTimeout({
            onDismiss(notification.id)
        }, notification.duration.toInt())
    }
    
    Div({
        classes("scale-in")
        style {
            backgroundColor(FlagentTheme.WorkspaceCardBorder)
            borderRadius(8.px)
            padding(16.px)
            property("backdrop-filter", "blur(12px)")
            property("border", "1px solid ${FlagentTheme.cssVar("color-dark-inputBorder")}")
            property("box-shadow", "0 4px 12px ${FlagentTheme.ShadowHover}")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.FlexStart)
            gap(12.px)
            borderLeft(4.px, LineStyle.Solid, getNotificationColor(notification.type))
        }
    }) {
        // Icon (with flex-shrink via wrapper)
        Span({
            style {
                flexShrink(0)
            }
        }) {
            Icon(getNotificationIcon(notification.type), size = 20.px, color = getNotificationColor(notification.type))
        }
        
        // Message
        Div({
            style {
                flex(1)
                fontSize(14.px)
                color(FlagentTheme.CodeBackground)
            }
        }) {
            Text(notification.message)
        }
        
        // Close button
        Button({
            onClick { onDismiss(notification.id) }
            style {
                padding(0.px)
                backgroundColor(Color.transparent)
                border(0.px)
                cursor("pointer")
                color(FlagentTheme.textLight(themeMode))
                flexShrink(0)
            }
        }) {
            Icon("close", size = 18.px)
        }
    }
}

private fun getNotificationIcon(type: NotificationType): String {
    return when (type) {
        NotificationType.SUCCESS -> "check_circle"
        NotificationType.ERROR -> "error"
        NotificationType.WARNING -> "warning"
        NotificationType.INFO -> "info"
    }
}

private fun getNotificationColor(type: NotificationType): CSSColorValue {
    return when (type) {
        NotificationType.SUCCESS -> FlagentTheme.Success
        NotificationType.ERROR -> FlagentTheme.Error
        NotificationType.WARNING -> FlagentTheme.Warning
        NotificationType.INFO -> FlagentTheme.Info
    }
}
