package flagent.frontend.components.settings

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.RoleResponse
import flagent.frontend.api.TenantUserResponse
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Roles & Permissions settings (Enterprise RBAC).
 */
@Composable
fun RolesSettings(themeMode: flagent.frontend.state.ThemeMode) {
    val roles = remember { mutableStateOf<List<RoleResponse>>(emptyList()) }
    val users = remember { mutableStateOf<List<TenantUserResponse>>(emptyList()) }
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    LaunchedEffect(Unit) {
        loading.value = true
        error.value = null
        try {
            roles.value = ApiClient.getRoles().roles
            users.value = ApiClient.getTenantUsers()
        } catch (e: Exception) {
            error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
        } finally {
            loading.value = false
        }
    }

    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight("600")
                marginBottom(20.px)
                color(FlagentTheme.text(themeMode))
            }
        }) {
            Text("Roles & Permissions")
        }
        P({
            style {
                color(FlagentTheme.textLight(themeMode))
                marginBottom(20.px)
            }
        }) {
            Text("Manage roles and assign permissions to users.")
        }
        if (error.value != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    marginBottom(15.px)
                }
            }) {
                Text(error.value!!)
            }
        }
        if (loading.value) {
            Div({ style { padding(20.px); textAlign("center") } }) { Text("Loading...") }
        } else {
            H3({
                style {
                    fontSize(16.px)
                    fontWeight("600")
                    marginBottom(12.px)
                    marginTop(24.px)
                    color(FlagentTheme.text(themeMode))
                }
            }) {
                Text("Available Roles")
            }
            for (role in roles.value) {
                Div({
                    style {
                        padding(12.px)
                        backgroundColor(FlagentTheme.BackgroundAlt)
                        borderRadius(6.px)
                        marginBottom(8.px)
                    }
                }) {
                    Span({ style { fontWeight("500") } }) { Text("${role.name} (${role.key})") }
                    if (role.isBuiltIn) {
                        Span({
                            style {
                                marginLeft(8.px)
                                fontSize(11.px)
                                padding(2.px, 6.px)
                                backgroundColor(Color("#E5E7EB"))
                                borderRadius(4.px)
                            }
                        }) { Text("Built-in") }
                    }
                    Div({ style { fontSize(13.px); color(FlagentTheme.textLight(themeMode)); marginTop(4.px) } }) {
                        Text(role.permissions.joinToString(", "))
                    }
                }
            }
            H3({
                style {
                    fontSize(16.px)
                    fontWeight("600")
                    marginBottom(12.px)
                    marginTop(24.px)
                    color(FlagentTheme.text(themeMode))
                }
            }) {
                Text("Users")
            }
            for (user in users.value) {
                Div({
                    style {
                        padding(12.px)
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        borderRadius(6.px)
                        marginBottom(8.px)
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.SpaceBetween)
                        alignItems(AlignItems.Center)
                    }
                }) {
                    Div {
                        Span({ style { fontWeight("500"); color(FlagentTheme.text(themeMode)) } }) { Text(user.email) }
                        Span({ style { fontSize(13.px); color(FlagentTheme.textLight(themeMode)); marginLeft(8.px) } }) {
                            Text("Role: ${user.role}")
                        }
                    }
                    val currentValue = when {
                        user.customRoleId != null -> "custom:${user.customRoleId}"
                        user.role == "OWNER" || user.role == "ADMIN" -> "ADMIN"
                        user.role == "MEMBER" -> "EDITOR"
                        user.role == "VIEWER" -> "VIEWER"
                        else -> user.role
                    }
                    Select({
                        onInput { event ->
                            val value = event.value ?: return@onInput
                            scope.launch {
                                try {
                                    if (value.startsWith("custom:")) {
                                        val id = value.removePrefix("custom:").toLongOrNull()
                                        if (id != null) ApiClient.assignUserRole(user.id, null, id)
                                    } else {
                                        ApiClient.assignUserRole(user.id, value, null)
                                    }
                                    users.value = ApiClient.getTenantUsers()
                                } catch (ex: Exception) {
                                    error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(ex))
                                }
                            }
                        }
                        style {
                            padding(6.px, 12.px)
                            borderRadius(4.px)
                            border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                            backgroundColor(FlagentTheme.inputBg(themeMode))
                            color(FlagentTheme.text(themeMode))
                            fontSize(13.px)
                        }
                    }) {
                        for (r in roles.value) {
                            val optValue = if (r.isBuiltIn) r.key else "custom:${r.id}"
                            val isSelected = (r.isBuiltIn && r.key == currentValue) || (r.isBuiltIn == false && r.id != null && "custom:${r.id}" == currentValue)
                            Option(optValue, { if (isSelected) selected() }) {
                                Text(r.name)
                            }
                        }
                    }
                }
            }
        }
    }
}
