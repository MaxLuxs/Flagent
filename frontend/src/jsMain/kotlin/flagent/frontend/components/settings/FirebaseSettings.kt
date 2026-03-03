package flagent.frontend.components.settings

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.FirebaseStatusResponse
import flagent.frontend.config.AppConfig
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import flagent.frontend.util.ErrorHandler
import flagent.frontend.util.triggerDownloadFromString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun FirebaseSettings(themeMode: ThemeMode) {
    val statusState = remember { mutableStateOf<FirebaseStatusResponse?>(null) }
    val loadingState = remember { mutableStateOf(true) }
    val errorState = remember { mutableStateOf<String?>(null) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    LaunchedEffect(Unit) {
        scope.launch {
            loadingState.value = true
            errorState.value = null
            ErrorHandler.withErrorHandling(
                block = {
                    statusState.value = ApiClient.getFirebaseStatus()
                },
                onError = { err -> errorState.value = ErrorHandler.getUserMessage(err) }
            )
            loadingState.value = false
        }
    }

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
        }) { Text(LocalizedStrings.firebaseSettingsTitle) }

        P({
            style {
                color(FlagentTheme.textLight(themeMode))
                marginBottom(20.px)
                fontSize(14.px)
            }
        }) {
            Text(LocalizedStrings.firebaseSettingsDescription)
        }

        if (errorState.value != null) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(FlagentTheme.errorBg(themeMode))
                    borderRadius(6.px)
                    color(FlagentTheme.errorText(themeMode))
                    marginBottom(16.px)
                }
            }) {
                Text(errorState.value!!)
            }
        } else if (loadingState.value || statusState.value == null) {
            Div({
                style {
                    padding(24.px)
                    textAlign("center")
                    color(FlagentTheme.textLight(themeMode))
                }
            }) { Text(LocalizedStrings.loading) }
        } else {
            val status = statusState.value!!

            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(260px, 1fr))")
                    gap(16.px)
                    marginBottom(24.px)
                }
            }) {
                // Firebase Remote Config
                Div({
                    style {
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        borderRadius(8.px)
                        padding(16.px)
                    }
                }) {
                    H3({
                        style {
                            fontSize(16.px)
                            fontWeight("600")
                            marginBottom(8.px)
                            color(FlagentTheme.text(themeMode))
                        }
                    }) { Text(LocalizedStrings.firebaseRcTitle) }
                    P({
                        style {
                            fontSize(13.px)
                            color(FlagentTheme.textLight(themeMode))
                            marginBottom(8.px)
                        }
                    }) {
                        Text(LocalizedStrings.firebaseRcDescription)
                    }
                    StatusRow(themeMode, enabled = status.firebaseRc.enabled)
                    DefinitionRow(
                        themeMode,
                        label = LocalizedStrings.firebaseProjectIdLabel,
                        value = status.firebaseRc.projectId ?: "—"
                    )
                    DefinitionRow(
                        themeMode,
                        label = LocalizedStrings.firebaseRcSyncIntervalLabel,
                        value = "${status.firebaseRc.syncIntervalSeconds}s"
                    )
                    DefinitionRow(
                        themeMode,
                        label = LocalizedStrings.firebaseRcPrefixLabel,
                        value = status.firebaseRc.parameterPrefix.ifBlank { "—" }
                    )
                    DefinitionRow(
                        themeMode,
                        label = LocalizedStrings.firebaseRcCredentialsLabel,
                        value = if (status.firebaseRc.hasCredentials) LocalizedStrings.configPresent else LocalizedStrings.configMissing
                    )
                }

                // Firebase Analytics
                Div({
                    style {
                        backgroundColor(FlagentTheme.inputBg(themeMode))
                        borderRadius(8.px)
                        padding(16.px)
                    }
                }) {
                    H3({
                        style {
                            fontSize(16.px)
                            fontWeight("600")
                            marginBottom(8.px)
                            color(FlagentTheme.text(themeMode))
                        }
                    }) { Text(LocalizedStrings.firebaseAnalyticsTitle) }
                    P({
                        style {
                            fontSize(13.px)
                            color(FlagentTheme.textLight(themeMode))
                            marginBottom(8.px)
                        }
                    }) {
                        Text(LocalizedStrings.firebaseAnalyticsDescription)
                    }
                    StatusRow(themeMode, enabled = status.firebaseAnalytics.enabled)
                    DefinitionRow(
                        themeMode,
                        label = LocalizedStrings.firebaseMeasurementIdLabel,
                        value = status.firebaseAnalytics.measurementId ?: "—"
                    )
                    DefinitionRow(
                        themeMode,
                        label = LocalizedStrings.firebaseApiSecretLabel,
                        value = if (status.firebaseAnalytics.hasApiSecret) LocalizedStrings.configPresent else LocalizedStrings.configMissing
                    )
                }
            }

            // Configuration / docs section
            Div({
                style {
                    paddingTop(16.px)
                    property("border-top", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                    marginTop(8.px)
                }
            }) {
                H3({
                    style {
                        fontSize(15.px)
                        fontWeight("600")
                        marginBottom(6.px)
                        color(FlagentTheme.text(themeMode))
                    }
                }) { Text(LocalizedStrings.firebaseConfigSectionTitle) }
                P({
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.textLight(themeMode))
                        marginBottom(8.px)
                    }
                }) {
                    Text(LocalizedStrings.firebaseConfigSectionDescription)
                }

                Ul({
                    style {
                        fontSize(13.px)
                        color(FlagentTheme.text(themeMode))
                        margin(0.px)
                        paddingLeft(18.px)
                    }
                }) {
                    listOf(
                        "FLAGENT_FIREBASE_RC_SYNC_ENABLED",
                        "FLAGENT_FIREBASE_RC_PROJECT_ID",
                        "FLAGENT_FIREBASE_RC_CREDENTIALS_JSON",
                        "FLAGENT_FIREBASE_RC_CREDENTIALS_FILE",
                        "FLAGENT_FIREBASE_RC_SYNC_INTERVAL",
                        "FLAGENT_FIREBASE_RC_PARAMETER_PREFIX",
                        "FLAGENT_FIREBASE_ANALYTICS_ENABLED",
                        "FLAGENT_FIREBASE_ANALYTICS_API_SECRET",
                        "FLAGENT_FIREBASE_ANALYTICS_MEASUREMENT_ID",
                        "FLAGENT_FIREBASE_ANALYTICS_APP_INSTANCE_ID_KEY",
                        "FLAGENT_FIREBASE_ANALYTICS_CLIENT_ID_KEY"
                    ).forEach { envName ->
                        Li {
                            Text(envName)
                        }
                    }
                }

                val docsUrl = "${AppConfig.docsUrl}#/guides/configuration?id=firebase-integration"

                A(href = docsUrl, attrs = {
                    style {
                        display(DisplayStyle.InlineBlock)
                        marginTop(12.px)
                        fontSize(13.px)
                        color(FlagentTheme.Primary)
                        textDecoration("underline")
                    }
                }) {
                    Text(LocalizedStrings.firebaseDocsLinkText)
                }
            }
        }
    }
}

@Composable
private fun StatusRow(themeMode: ThemeMode, enabled: Boolean) {
    Div({
        style {
            marginTop(8.px)
            marginBottom(4.px)
        }
    }) {
        Span({
            style {
                display(DisplayStyle.InlineBlock)
                padding(4.px, 8.px)
                borderRadius(999.px)
                fontSize(12.px)
                backgroundColor(
                    if (enabled) FlagentTheme.successBg(themeMode) else FlagentTheme.inputBg(themeMode)
                )
                color(
                    if (enabled) FlagentTheme.Success else FlagentTheme.textLight(themeMode)
                )
            }
        }) {
            Text(if (enabled) LocalizedStrings.enabled else LocalizedStrings.disabled)
        }
    }
}

@Composable
private fun DefinitionRow(themeMode: ThemeMode, label: String, value: String) {
    Div({
        style {
            marginTop(4.px)
        }
    }) {
        Span({
            style {
                fontSize(12.px)
                color(FlagentTheme.textLight(themeMode))
                marginRight(4.px)
            }
        }) { Text(label) }
        Span({
            style {
                fontSize(13.px)
                color(FlagentTheme.text(themeMode))
            }
        }) { Text(value) }
    }
}

