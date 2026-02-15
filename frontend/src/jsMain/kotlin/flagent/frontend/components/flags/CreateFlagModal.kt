package flagent.frontend.components.flags

import androidx.compose.runtime.Composable
import flagent.frontend.components.Icon
import flagent.frontend.i18n.LocalizedStrings
import flagent.frontend.state.LocalThemeMode
import flagent.frontend.state.ThemeMode
import flagent.frontend.theme.FlagentTheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * Modal for choosing the type of flag to create.
 * Replaces the unclear "More options" dropdown with clear cards:
 * - Boolean flag (simple on/off, uses template simple_boolean_flag)
 * - Full form (navigate to create page, manual description/key and full config)
 */
@Composable
fun CreateFlagModal(
    onClose: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onSelectFullForm: () -> Unit
) {
    val themeMode = LocalThemeMode.current
    Div({
        style {
            position(Position.Fixed)
            top(0.px)
            left(0.px)
            right(0.px)
            bottom(0.px)
            property("background-color", "rgba(0,0,0,0.5)")
            property("backdrop-filter", "blur(8px)")
            property("-webkit-backdrop-filter", "blur(8px)")
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            property("z-index", "1000")
        }
        onClick { event ->
            if (event.target == event.currentTarget) onClose()
        }
    }) {
        Div({
            style {
                backgroundColor(FlagentTheme.cardBg(themeMode))
                property("backdrop-filter", "blur(12px)")
                property("-webkit-backdrop-filter", "blur(12px)")
                padding(28.px)
                borderRadius(12.px)
                width(100.percent)
                property("max-width", "520px")
                border(1.px, LineStyle.Solid, FlagentTheme.cardBorder(themeMode))
                property("box-shadow", "0 10px 25px rgba(0, 0, 0, 0.3)")
            }
            onClick { it.stopPropagation() }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                    marginBottom(20.px)
                    property("border-bottom", "1px solid ${FlagentTheme.cardBorder(themeMode)}")
                    paddingBottom(16.px)
                }
            }) {
                H3({
                    style {
                        margin(0.px)
                        color(FlagentTheme.text(themeMode))
                        fontSize(20.px)
                        fontWeight("600")
                    }
                }) {
                    Text(LocalizedStrings.createFlagModalTitle)
                }
                Button({
                    onClick { onClose() }
                    style {
                        padding(4.px, 8.px)
                        property("background-color", "transparent")
                        color(FlagentTheme.textLight(themeMode))
                        border { width(0.px); style(LineStyle.None) }
                        borderRadius(4.px)
                        cursor("pointer")
                        fontSize(20.px)
                        property("line-height", "1")
                        property("transition", "color 0.2s")
                    }
                    onMouseEnter {
                        (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.text(themeMode).toString()
                    }
                    onMouseLeave {
                        (it.target as org.w3c.dom.HTMLElement).style.color = FlagentTheme.textLight(themeMode).toString()
                    }
                }) {
                    Text("×")
                }
            }
            Div({
                style {
                    display(DisplayStyle.Grid)
                    property("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                    gap(16.px)
                }
            }) {
                CreateFlagTypeCard(
                    themeMode = themeMode,
                    iconName = "toggle_on",
                    title = LocalizedStrings.createFlagTypeBooleanTitle,
                    description = LocalizedStrings.createFlagTypeBooleanDescription,
                    onClick = {
                        onSelectTemplate("simple_boolean_flag")
                        onClose()
                    }
                )
                CreateFlagTypeCard(
                    themeMode = themeMode,
                    iconName = "science",
                    title = LocalizedStrings.createFlagTypeAbTestTitle,
                    description = LocalizedStrings.createFlagTypeAbTestDescription,
                    onClick = {
                        onSelectFullForm()
                        onClose()
                    }
                )
                CreateFlagTypeCard(
                    themeMode = themeMode,
                    iconName = "pin",
                    title = LocalizedStrings.createFlagTypeNumericTitle,
                    description = LocalizedStrings.createFlagTypeNumericDescription,
                    onClick = {
                        onSelectFullForm()
                        onClose()
                    }
                )
                CreateFlagTypeCard(
                    themeMode = themeMode,
                    iconName = "code",
                    title = LocalizedStrings.createFlagTypeStringJsonTitle,
                    description = LocalizedStrings.createFlagTypeStringJsonDescription,
                    onClick = {
                        onSelectFullForm()
                        onClose()
                    }
                )
                CreateFlagTypeCard(
                    themeMode = themeMode,
                    iconName = "edit_note",
                    title = LocalizedStrings.createFlagTypeFullFormTitle,
                    description = LocalizedStrings.createFlagTypeFullFormDescription,
                    onClick = {
                        onSelectFullForm()
                        onClose()
                    }
                )
            }
        }
    }
}

@Composable
private fun CreateFlagTypeCard(
    themeMode: ThemeMode,
    iconName: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    // In dark mode use a bit more opaque card background so text stays readable (like command bar search)
    val cardBg = when (themeMode) {
        ThemeMode.Dark -> Color("rgba(255,255,255,0.12)")
        ThemeMode.Light -> FlagentTheme.inputBg(themeMode)
    }
    val descriptionColor = when (themeMode) {
        ThemeMode.Dark -> Color("rgba(255,255,255,0.88)")
        ThemeMode.Light -> FlagentTheme.textLight(themeMode)
    }
    Div({
        style {
            padding(20.px)
            backgroundColor(cardBg)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(FlagentTheme.cardBorder(themeMode))
            }
            borderRadius(8.px)
            cursor("pointer")
            property("transition", "all 0.2s")
        }
        onClick { onClick() }
        onMouseEnter {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.borderColor = FlagentTheme.Primary.toString()
            el.style.setProperty("box-shadow", "0 4px 12px ${FlagentTheme.ShadowHover}")
        }
        onMouseLeave {
            val el = it.target as org.w3c.dom.HTMLElement
            el.style.borderColor = FlagentTheme.cardBorder(themeMode).toString()
            el.style.boxShadow = "none"
        }
    }) {
        Icon(
            name = iconName,
            size = 28.px,
            color = FlagentTheme.Primary
        )
        Div({
            style {
                marginTop(12.px)
                marginBottom(6.px)
                color(FlagentTheme.text(themeMode))
                fontSize(16.px)
                fontWeight("600")
            }
        }) {
            Text(title)
        }
        Div({
            style {
                color(descriptionColor)
                fontSize(13.px)
                property("line-height", "1.4")
            }
        }) {
            Text(description)
        }
    }
}
