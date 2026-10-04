package com.proteintracker.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colours that Material 3 does not define (success / warning) plus the
 * paired "on" colours needed to keep the calendar heat map legible.
 *
 * Dynamic Color can only restyle M3 roles, so the heat-map bands live here and
 * are swapped on dark/light rather than derived from the wallpaper.
 */
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val neutralContainer: Color,
    val onNeutralContainer: Color,
)

internal val ExtendedLight = ExtendedColors(
    success = GreenLight,
    onSuccess = GreenOnLight,
    successContainer = GreenContainerLight,
    onSuccessContainer = OnGreenContainerLight,
    warning = Color(0xFF7A5900),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFDF9A),
    onWarningContainer = Color(0xFF261A00),
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    neutralContainer = Color(0xFFDEE5D9),
    onNeutralContainer = Color(0xFF424940),
)

internal val ExtendedDark = ExtendedColors(
    success = GreenDark,
    onSuccess = GreenOnDark,
    successContainer = GreenContainerDark,
    onSuccessContainer = OnGreenContainerDark,
    warning = Color(0xFFF5BF48),
    onWarning = Color(0xFF412D00),
    warningContainer = Color(0xFF5C4200),
    onWarningContainer = Color(0xFFFFDF9A),
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    neutralContainer = Color(0xFF414940),
    onNeutralContainer = Color(0xFFC2C9BD),
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedLight }
