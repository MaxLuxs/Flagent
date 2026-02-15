package flagent.frontend.components.settings

import androidx.compose.runtime.*
import flagent.frontend.api.AdminUserResponse
import flagent.frontend.api.ApiClient
import flagent.frontend.api.CreateAdminUserRequest
import flagent.frontend.api.UpdateAdminUserRequest
import flagent.frontend.components.Icon
import flagent.frontend.components.Modal
import flagent.frontend.components.common.ConfirmDialog
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.attributes.value
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Admin users settings: list, add, edit, block, unblock, delete.
 */
@Composable
fun AdminUsersSettings(themeMode: ThemeMode) {
    val users = remember { mutableStateOf<List<AdminUserResponse>>(emptyList()) }
    val totalCount = remember { mutableStateOf(0L) }
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    fun loadUsers() {
        scope.launch {
            loading.value = true
            error.value = null
            try {
                val (list, total) = ApiClient.getAdminUsers(limit = 50, offset = 0)
                users.value = list
                totalCount.value = total
            } catch (e: Exception) {
                error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
            } finally {
                loading.value = false
            }
        }
    }

    LaunchedEffect(Unit) { loadUsers() }

    var showAddModal by remember { mutableStateOf(false) }
    var editUser by remember { mutableStateOf<AdminUserResponse?>(null) }
    var confirmBlock by remember { mutableStateOf<AdminUserResponse?>(null) }
    var confirmDelete by remember { mutableStateOf<AdminUserResponse?>(null) }

    Div({
        style {
            backgroundColor(FlagentTheme.cardBg(themeMode))
            borderRadius(8.px)
            padding(20.px)
            property("box-shadow", FlagentTheme.ShadowCard)
        }
    }) {
        H2({
            style {
                fontSize(20.px)
                fontWeight("600")
                marginBottom(8.px)
                color(FlagentTheme.text(themeMode))
            }
        }) { Text(LocalizedStrings.adminUsersTab) }
        P({
            style {
                color(FlagentTheme.textLight(themeMode))
                marginBottom(20.px)
                fontSize(14.px)
            }
        }) { Text(LocalizedStrings.adminUsersDescription) }

        if (error.value != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    marginBottom(15.px)
                }
            }) { Text(error.value!!) }
        }

        Button({
            onClick { showAddModal = true }
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(8.px)
                padding(10.px, 16.px)
                backgroundColor(FlagentTheme.Primary)
                color(Color.white)
                border(0.px)
                borderRadius(6.px)
                cursor("pointer")
                fontSize(14.px)
                fontWeight("500")
            }
        }) {
            Icon("person_add", size = 18.px, color = Color.white)
            Text(LocalizedStrings.addUser)
        }

        if (loading.value) {
            Div({
                style {
                    padding(24.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) { Text(LocalizedStrings.loading) }
        } else if (users.value.isEmpty()) {
            Div({
                style {
                    padding(24.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) { Text("No users yet. Add one to get started.") }
        } else {
            Div({
                style {
                    marginTop(20.px)
                    property("border", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                    borderRadius(6.px)
                    overflow("hidden")
                }
            }) {
                users.value.forEach { user ->
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            justifyContent(JustifyContent.SpaceBetween)
                            padding(12.px, 16.px)
                            property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                        }
                    }) {
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                gap(4.px)
                            }
                        }) {
                            Span({
                                style {
                                    fontWeight("500")
                                    color(FlagentTheme.text(themeMode))
                                    fontSize(14.px)
                                }
                            }) { Text(user.email ?: "—") }
                            Span({
                                style {
                                    fontSize(13.px)
                                    color(FlagentTheme.textLight(themeMode))
                                }
                            }) {
                                Text(
                                    (user.name?.takeIf { it.isNotBlank() } ?: "—") +
                                        " • " +
                                        if (user.blockedAt != null) LocalizedStrings.userStatusBlocked
                                        else LocalizedStrings.userStatusActive +
                                            (user.createdAt?.let { " • ${it.take(10)}" } ?: "")
                                )
                            }
                        }
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                gap(8.px)
                            }
                        }) {
                            Button({
                                onClick { editUser = user }
                                style {
                                    display(DisplayStyle.Flex); alignItems(AlignItems.Center)
                                    padding(6.px, 10.px); border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                                    borderRadius(6.px); backgroundColor(FlagentTheme.inputBg(themeMode))
                                    color(FlagentTheme.text(themeMode)); cursor("pointer"); fontSize(13.px)
                                }
                            }) {
                                Icon("edit", size = 16.px, color = FlagentTheme.textLight(themeMode))
                                Span({ style { marginLeft(4.px); fontSize(13.px) } }) { Text(LocalizedStrings.editUser) }
                            }
                            if (user.blockedAt != null) {
                                Button({
                                    onClick { confirmBlock = user }
                                    style {
                                        display(DisplayStyle.Flex); alignItems(AlignItems.Center)
                                        padding(6.px, 10.px); border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                                        borderRadius(6.px); backgroundColor(FlagentTheme.inputBg(themeMode))
                                        color(FlagentTheme.text(themeMode)); cursor("pointer"); fontSize(13.px)
                                    }
                                }) {
                                    Icon("lock_open", size = 16.px, color = FlagentTheme.textLight(themeMode))
                                    Span({ style { marginLeft(4.px); fontSize(13.px) } }) { Text(LocalizedStrings.unblockUser) }
                                }
                            } else {
                                Button({
                                    onClick { confirmBlock = user }
                                    style {
                                        display(DisplayStyle.Flex); alignItems(AlignItems.Center)
                                        padding(6.px, 10.px); border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                                        borderRadius(6.px); backgroundColor(FlagentTheme.inputBg(themeMode))
                                        color(FlagentTheme.text(themeMode)); cursor("pointer"); fontSize(13.px)
                                    }
                                }) {
                                    Icon("block", size = 16.px, color = FlagentTheme.textLight(themeMode))
                                    Span({ style { marginLeft(4.px); fontSize(13.px) } }) { Text(LocalizedStrings.blockUser) }
                                }
                            }
                            Button({
                                onClick { confirmDelete = user }
                                style {
                                    display(DisplayStyle.Flex); alignItems(AlignItems.Center)
                                    padding(6.px, 10.px); border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                                    borderRadius(6.px); backgroundColor(FlagentTheme.inputBg(themeMode))
                                    color(FlagentTheme.errorText(themeMode)); cursor("pointer"); fontSize(13.px)
                                }
                            }) {
                                Icon("delete", size = 16.px, color = FlagentTheme.errorText(themeMode))
                                Span({ style { marginLeft(4.px); fontSize(13.px) } }) { Text(LocalizedStrings.deleteUser) }
                            }
                        }
                    }
                }
            }
            if (totalCount.value > users.value.size) {
                Div({
                    style {
                        marginTop(8.px)
                        fontSize(13.px)
                        color(FlagentTheme.textLight(themeMode))
                    }
                }) { Text("Showing ${users.value.size} of ${totalCount.value}") }
            }
        }
    }

    if (showAddModal) AddUserModal(themeMode, onClose = { showAddModal = false }, onSuccess = { loadUsers(); showAddModal = false }, onError = { error.value = it })
    editUser?.let { u ->
        EditUserModal(themeMode, user = u, onClose = { editUser = null }, onSuccess = { loadUsers(); editUser = null }, onError = { error.value = it })
    }
    confirmBlock?.let { u ->
        ConfirmDialog(
            isOpen = true,
            title = if (u.blockedAt != null) LocalizedStrings.confirmUnblockUserTitle else LocalizedStrings.confirmBlockUserTitle,
            message = if (u.blockedAt != null) LocalizedStrings.confirmUnblockUserTitle else LocalizedStrings.confirmBlockUserMessage,
            confirmLabel = if (u.blockedAt != null) LocalizedStrings.unblockUser else LocalizedStrings.blockUser,
            isDangerous = u.blockedAt == null,
            onConfirm = {
                scope.launch {
                    try {
                        if (u.blockedAt != null) ApiClient.unblockAdminUser(u.id) else ApiClient.blockAdminUser(u.id)
                        loadUsers()
                    } catch (e: Exception) {
                        error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                    }
                    confirmBlock = null
                }
            },
            onCancel = { confirmBlock = null }
        )
    }
    confirmDelete?.let { u ->
        ConfirmDialog(
            isOpen = true,
            title = LocalizedStrings.confirmDeleteUserTitle,
            message = LocalizedStrings.confirmDeleteUserMessage,
            confirmLabel = LocalizedStrings.deleteUser,
            isDangerous = true,
            onConfirm = {
                scope.launch {
                    try {
                        ApiClient.deleteAdminUser(u.id)
                        loadUsers()
                    } catch (e: Exception) {
                        error.value = ErrorHandler.getUserMessage(ErrorHandler.handle(e))
                    }
                    confirmDelete = null
                }
            },
            onCancel = { confirmDelete = null }
        )
    }
}

@Composable
private fun AddUserModal(themeMode: ThemeMode, onClose: () -> Unit, onSuccess: () -> Unit, onError: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    Modal(title = LocalizedStrings.addUser, onClose = onClose, showCancel = false, width = 420.px, content = {
        Div({ style { padding(0.px) } }) {
            Input(InputType.Email) {
                value(email)
                onInput { email = it.value }
                placeholder(LocalizedStrings.emailPlaceholder)
                style {
                    width(100.percent); padding(10.px, 12.px); borderRadius(6.px)
                    border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                    backgroundColor(FlagentTheme.inputBg(themeMode)); color(FlagentTheme.text(themeMode)); fontSize(14.px)
                }
            }
            Input(InputType.Password) {
                value(password)
                onInput { password = it.value }
                placeholder(LocalizedStrings.passwordPlaceholder)
                style {
                    width(100.percent); padding(10.px, 12.px); marginTop(12.px); borderRadius(6.px)
                    border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                    backgroundColor(FlagentTheme.inputBg(themeMode)); color(FlagentTheme.text(themeMode)); fontSize(14.px)
                }
            }
            Input(InputType.Text) {
                value(name)
                onInput { name = it.value }
                placeholder(LocalizedStrings.namePlaceholder)
                style {
                    width(100.percent); padding(10.px, 12.px); marginTop(12.px); borderRadius(6.px)
                    border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                    backgroundColor(FlagentTheme.inputBg(themeMode)); color(FlagentTheme.text(themeMode)); fontSize(14.px)
                }
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.FlexEnd)
                    gap(12.px)
                    marginTop(24.px)
                }
            }) {
                Button({
                    onClick { onClose() }
                    style {
                        padding(10.px, 20.px); backgroundColor(FlagentTheme.inputBg(themeMode)); color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode)); borderRadius(6.px); cursor("pointer"); fontSize(14.px)
                    }
                }) { Text(LocalizedStrings.cancel) }
                Button({
                    onClick {
                        if (!email.isBlank() && !password.isBlank()) {
                            saving = true
                            scope.launch {
                                try {
                                    ApiClient.createAdminUser(email.trim(), password, name.takeIf { it.isNotBlank() })
                                    onSuccess()
                                } catch (e: Exception) {
                                    onError(ErrorHandler.getUserMessage(ErrorHandler.handle(e)))
                                } finally {
                                    saving = false
                                }
                            }
                        } else {
                            onError("Email and password are required")
                        }
                    }
                    style {
                        padding(10.px, 20.px); backgroundColor(FlagentTheme.Primary); color(Color.white)
                        border(0.px); borderRadius(6.px); cursor("pointer"); fontSize(14.px)
                        if (saving) property("opacity", "0.7")
                    }
                }) { Text(if (saving) LocalizedStrings.saving else LocalizedStrings.save) }
            }
        }
    })
}

@Composable
private fun EditUserModal(themeMode: ThemeMode, user: AdminUserResponse, onClose: () -> Unit, onSuccess: () -> Unit, onError: (String) -> Unit) {
    var name by remember(user.id) { mutableStateOf(user.name ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    Modal(title = LocalizedStrings.editUser + " ${user.email}", onClose = onClose, showCancel = false, width = 420.px, content = {
        Div({ style { padding(0.px) } }) {
            Input(InputType.Text) {
                value(name)
                onInput { name = it.value }
                placeholder(LocalizedStrings.namePlaceholder)
                style {
                    width(100.percent); padding(10.px, 12.px); borderRadius(6.px)
                    border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                    backgroundColor(FlagentTheme.inputBg(themeMode)); color(FlagentTheme.text(themeMode)); fontSize(14.px)
                }
            }
            Input(InputType.Password) {
                value(newPassword)
                onInput { newPassword = it.value }
                placeholder(LocalizedStrings.newPasswordPlaceholder)
                style {
                    width(100.percent); padding(10.px, 12.px); marginTop(12.px); borderRadius(6.px)
                    border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode))
                    backgroundColor(FlagentTheme.inputBg(themeMode)); color(FlagentTheme.text(themeMode)); fontSize(14.px)
                }
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.FlexEnd)
                    gap(12.px)
                    marginTop(24.px)
                }
            }) {
                Button({
                    onClick { onClose() }
                    style {
                        padding(10.px, 20.px); backgroundColor(FlagentTheme.inputBg(themeMode)); color(FlagentTheme.text(themeMode))
                        border(1.px, LineStyle.Solid, FlagentTheme.inputBorder(themeMode)); borderRadius(6.px); cursor("pointer"); fontSize(14.px)
                    }
                }) { Text(LocalizedStrings.cancel) }
                Button({
                    onClick {
                        saving = true
                        scope.launch {
                            try {
                                ApiClient.updateAdminUser(user.id, name.takeIf { it.isNotBlank() }, newPassword.takeIf { it.isNotBlank() })
                                onSuccess()
                            } catch (e: Exception) {
                                onError(ErrorHandler.getUserMessage(ErrorHandler.handle(e)))
                            } finally {
                                saving = false
                            }
                        }
                    }
                    style {
                        padding(10.px, 20.px); backgroundColor(FlagentTheme.Primary); color(Color.white)
                        border(0.px); borderRadius(6.px); cursor("pointer"); fontSize(14.px)
                        if (saving) property("opacity", "0.7")
                    }
                }) { Text(if (saving) LocalizedStrings.saving else LocalizedStrings.save) }
            }
        }
    })
}

