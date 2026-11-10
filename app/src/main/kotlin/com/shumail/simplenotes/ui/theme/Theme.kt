package com.shumail.simplenotes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shumail.simplenotes.R
import com.shumail.simplenotes.model.AccentPalette
import com.shumail.simplenotes.model.EditorFont
import com.shumail.simplenotes.model.ThemeMode

val InstrumentSerifFamily = FontFamily(
    Font(R.font.instrument_serif, FontWeight.Normal)
)

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium)
)

fun editorFontFamilyFor(editorFont: EditorFont): FontFamily = when (editorFont) {
    EditorFont.JETBRAINS_MONO -> JetBrainsMonoFamily
    EditorFont.PLUS_JAKARTA_SANS -> PlusJakartaSansFamily
    EditorFont.INSTRUMENT_SERIF -> InstrumentSerifFamily
}

val SimpleNotesTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.15).sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)

val SimpleNotesShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

private fun buildSeededColorScheme(palette: AccentPalette, isDark: Boolean): ColorScheme {
    data class PaletteSpec(
        val lightPrimary: Color,
        val lightPrimaryContainer: Color,
        val lightOnPrimaryContainer: Color,
        val lightSecondary: Color,
        val lightSecondaryContainer: Color,
        val lightTertiary: Color,
        val darkPrimary: Color,
        val darkPrimaryContainer: Color,
        val darkOnPrimaryContainer: Color,
        val darkSecondary: Color,
        val darkSecondaryContainer: Color,
        val darkTertiary: Color
    )

    val spec = when (palette) {
        AccentPalette.MATERIAL_YOU, AccentPalette.FLUENT_SAGE -> PaletteSpec(
            lightPrimary = Color(0xFF1F634C),
            lightPrimaryContainer = Color(0xFFD2F0E2),
            lightOnPrimaryContainer = Color(0xFF0A2E21),
            lightSecondary = Color(0xFF456155),
            lightSecondaryContainer = Color(0xFFCFE6DB),
            lightTertiary = Color(0xFF3A6275),
            darkPrimary = Color(0xFF8CD6B5),
            darkPrimaryContainer = Color(0xFF164B38),
            darkOnPrimaryContainer = Color(0xFFBDF2D9),
            darkSecondary = Color(0xFFB1CCC0),
            darkSecondaryContainer = Color(0xFF2E473D),
            darkTertiary = Color(0xFFA2C9DD)
        )
        AccentPalette.FLUENT_COBALT -> PaletteSpec(
            lightPrimary = Color(0xFF1D56C9),
            lightPrimaryContainer = Color(0xFFD9E4FF),
            lightOnPrimaryContainer = Color(0xFF08225E),
            lightSecondary = Color(0xFF495D82),
            lightSecondaryContainer = Color(0xFFD7E2F9),
            lightTertiary = Color(0xFF0E6B7B),
            darkPrimary = Color(0xFF9EC0FF),
            darkPrimaryContainer = Color(0xFF143E96),
            darkOnPrimaryContainer = Color(0xFFDCE7FF),
            darkSecondary = Color(0xFFB5C7E8),
            darkSecondaryContainer = Color(0xFF2E4163),
            darkTertiary = Color(0xFF82D3E3)
        )
        AccentPalette.TERRACOTTA -> PaletteSpec(
            lightPrimary = Color(0xFFA8432B),
            lightPrimaryContainer = Color(0xFFFFDBD2),
            lightOnPrimaryContainer = Color(0xFF3B0B02),
            lightSecondary = Color(0xFF75574E),
            lightSecondaryContainer = Color(0xFFFFDBD1),
            lightTertiary = Color(0xFF6A5E2F),
            darkPrimary = Color(0xFFFFB4A1),
            darkPrimaryContainer = Color(0xFF7D2C17),
            darkOnPrimaryContainer = Color(0xFFFFDBD2),
            darkSecondary = Color(0xFFE7BDB2),
            darkSecondaryContainer = Color(0xFF5C4037),
            darkTertiary = Color(0xFFD7C68D)
        )
        AccentPalette.NORD_FROST -> PaletteSpec(
            lightPrimary = Color(0xFF286986),
            lightPrimaryContainer = Color(0xFFCDE9F8),
            lightOnPrimaryContainer = Color(0xFF052636),
            lightSecondary = Color(0xFF4A626E),
            lightSecondaryContainer = Color(0xFFCFE6F2),
            lightTertiary = Color(0xFF565B7E),
            darkPrimary = Color(0xFF88C8E8),
            darkPrimaryContainer = Color(0xFF134D66),
            darkOnPrimaryContainer = Color(0xFFD2ECFA),
            darkSecondary = Color(0xFFB4CAD6),
            darkSecondaryContainer = Color(0xFF334A55),
            darkTertiary = Color(0xFFBEC3EB)
        )
        AccentPalette.AMBER_INK -> PaletteSpec(
            lightPrimary = Color(0xFF8C5E08),
            lightPrimaryContainer = Color(0xFFFFDFB0),
            lightOnPrimaryContainer = Color(0xFF2D1B00),
            lightSecondary = Color(0xFF6D5C43),
            lightSecondaryContainer = Color(0xFFF7DFBF),
            lightTertiary = Color(0xFF4C6545),
            darkPrimary = Color(0xFFF6BE5C),
            darkPrimaryContainer = Color(0xFF664300),
            darkOnPrimaryContainer = Color(0xFFFFDFB0),
            darkSecondary = Color(0xFFDAC3A4),
            darkSecondaryContainer = Color(0xFF54452E),
            darkTertiary = Color(0xFFB3CEA9)
        )
        AccentPalette.ROSE_QUARTZ -> PaletteSpec(
            lightPrimary = Color(0xFF8D3B63),
            lightPrimaryContainer = Color(0xFFFFD8E6),
            lightOnPrimaryContainer = Color(0xFF390720),
            lightSecondary = Color(0xFF725762),
            lightSecondaryContainer = Color(0xFFFCD9E6),
            lightTertiary = Color(0xFF7E5539),
            darkPrimary = Color(0xFFFFB0D0),
            darkPrimaryContainer = Color(0xFF6E234A),
            darkOnPrimaryContainer = Color(0xFFFFD8E6),
            darkSecondary = Color(0xFFE0BDCB),
            darkSecondaryContainer = Color(0xFF58404A),
            darkTertiary = Color(0xFFF1BB98)
        )
    }

    return if (isDark) {
        darkColorScheme(
            primary = spec.darkPrimary,
            onPrimary = Color(0xFF0A1F17),
            primaryContainer = spec.darkPrimaryContainer,
            onPrimaryContainer = spec.darkOnPrimaryContainer,
            secondary = spec.darkSecondary,
            onSecondary = Color(0xFF192620),
            secondaryContainer = spec.darkSecondaryContainer,
            onSecondaryContainer = Color(0xFFE5F2EC),
            tertiary = spec.darkTertiary,
            background = Color(0xFF111417),
            onBackground = Color(0xFFE6E8EA),
            surface = Color(0xFF111417),
            onSurface = Color(0xFFE6E8EA),
            surfaceVariant = Color(0xFF23292F),
            onSurfaceVariant = Color(0xFFBDC5CC),
            surfaceContainerLowest = Color(0xFF0C0E11),
            surfaceContainerLow = Color(0xFF161A1E),
            surfaceContainer = Color(0xFF1B2025),
            surfaceContainerHigh = Color(0xFF232930),
            surfaceContainerHighest = Color(0xFF2D343C),
            outline = Color(0xFF6E7780),
            outlineVariant = Color(0xFF323940)
        )
    } else {
        lightColorScheme(
            primary = spec.lightPrimary,
            onPrimary = Color.White,
            primaryContainer = spec.lightPrimaryContainer,
            onPrimaryContainer = spec.lightOnPrimaryContainer,
            secondary = spec.lightSecondary,
            onSecondary = Color.White,
            secondaryContainer = spec.lightSecondaryContainer,
            onSecondaryContainer = Color(0xFF12231B),
            tertiary = spec.lightTertiary,
            background = Color(0xFFFBF9F5),
            onBackground = Color(0xFF181C20),
            surface = Color(0xFFFBF9F5),
            onSurface = Color(0xFF181C20),
            surfaceVariant = Color(0xFFE9E6DF),
            onSurfaceVariant = Color(0xFF444B52),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF5F3EE),
            surfaceContainer = Color(0xFFEFEDE7),
            surfaceContainerHigh = Color(0xFFE8E5DE),
            surfaceContainerHighest = Color(0xFFE0DDD5),
            outline = Color(0xFF747B82),
            outlineVariant = Color(0xFFCFD3D7)
        )
    }
}

private fun ColorScheme.toAmoledScheme(): ColorScheme = this.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF090B0D),
    surfaceContainer = Color(0xFF101317),
    surfaceContainerHigh = Color(0xFF181C22),
    surfaceContainerHighest = Color(0xFF21262E),
    outlineVariant = Color(0xFF242A30)
)

@Composable
fun SimpleNotesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentPalette: AccentPalette = AccentPalette.MATERIAL_YOU,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }

    val baseScheme = if (accentPalette == AccentPalette.MATERIAL_YOU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        buildSeededColorScheme(accentPalette, useDark)
    }

    val finalScheme = if (themeMode == ThemeMode.AMOLED) {
        baseScheme.toAmoledScheme()
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = finalScheme,
        typography = SimpleNotesTypography,
        shapes = SimpleNotesShapes,
        content = content
    )
}
