package ke.eelaminnovations.kangaishop.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ke.eelaminnovations.kangaishop.domain.model.AppTheme

// 1. Brand Blue Scheme Colors
private val BrandLightColorScheme = lightColorScheme(
    primary = Color(0xFF1A56A0),
    primaryContainer = Color(0xFFD6E4F7),
    secondary = Color(0xFFE07B00),
    secondaryContainer = Color(0xFFFFDDB3),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEF2F7),
    error = Color(0xFFD32F2F),
    onPrimary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E),
    outline = Color(0xFFB0BEC5)
)

private val BrandDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90B8F8),
    primaryContainer = Color(0xFF004880),
    secondary = Color(0xFFFFB74D),
    secondaryContainer = Color(0xFF774000),
    background = Color(0xFF000000),
    surface = Color(0xFF0B111D),
    surfaceVariant = Color(0xFF10192A),
    error = Color(0xFFEF9A9A),
    onPrimary = Color(0xFF00316E),
    onBackground = Color(0xFFE1E1E1),
    onSurface = Color(0xFFE1E1E1),
    outline = Color(0xFF4A4A4A)
)

// 2. Forest Green Scheme
private val ForestLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    primaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF4CAF50),
    secondaryContainer = Color(0xFFE8F5E9),
    background = Color(0xFFF1F8E9),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8F5E9),
    error = Color(0xFFD32F2F)
)
private val ForestDarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    primaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFFA5D6A7),
    secondaryContainer = Color(0xFF2E7D32),
    background = Color(0xFF000000),
    surface = Color(0xFF0A140B),
    surfaceVariant = Color(0xFF0E1F10),
    error = Color(0xFFEF9A9A)
)

// 3. Sunset Gold Scheme
private val SunsetLightColorScheme = lightColorScheme(
    primary = Color(0xFFE65100),
    primaryContainer = Color(0xFFFFCC80),
    secondary = Color(0xFFF57C00),
    secondaryContainer = Color(0xFFFFE0B2),
    background = Color(0xFFFFF3E0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFE0B2),
    error = Color(0xFFD32F2F)
)
private val SunsetDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB74D),
    primaryContainer = Color(0xFFE65100),
    secondary = Color(0xFFFFCC80),
    secondaryContainer = Color(0xFFF57C00),
    background = Color(0xFF000000),
    surface = Color(0xFF140F06),
    surfaceVariant = Color(0xFF1F170A),
    error = Color(0xFFEF9A9A)
)

// 4. Ocean Breeze Scheme
private val OceanLightColorScheme = lightColorScheme(
    primary = Color(0xFF00796B),
    primaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFF009688),
    secondaryContainer = Color(0xFFE0F2F1),
    background = Color(0xFFE0F2F1),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0F2F1),
    error = Color(0xFFD32F2F)
)
private val OceanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    primaryContainer = Color(0xFF004D40),
    secondary = Color(0xFF80CBC4),
    secondaryContainer = Color(0xFF00796B),
    background = Color(0xFF000000),
    surface = Color(0xFF061211),
    surfaceVariant = Color(0xFF0A1C1A),
    error = Color(0xFFEF9A9A)
)

// 5. Lavender Field Scheme
private val LavenderLightColorScheme = lightColorScheme(
    primary = Color(0xFF673AB7),
    primaryContainer = Color(0xFFD1C4E9),
    secondary = Color(0xFF9575CD),
    secondaryContainer = Color(0xFFF3E5F5),
    background = Color(0xFFF3E5F5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF3E5F5),
    error = Color(0xFFD32F2F)
)
private val LavenderDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB39DDB),
    primaryContainer = Color(0xFF311B92),
    secondary = Color(0xFFD1C4E9),
    secondaryContainer = Color(0xFF512DA8),
    background = Color(0xFF000000),
    surface = Color(0xFF0F0E16),
    surfaceVariant = Color(0xFF161521),
    error = Color(0xFFEF9A9A)
)

// 6. Crimson Velvet Scheme
private val CrimsonLightColorScheme = lightColorScheme(
    primary = Color(0xFF880E4F),
    primaryContainer = Color(0xFFF8BBD0),
    secondary = Color(0xFFD81B60),
    secondaryContainer = Color(0xFFFCE4EC),
    background = Color(0xFFFCE4EC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFCE4EC),
    error = Color(0xFFD32F2F)
)
private val CrimsonDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF48FB1),
    primaryContainer = Color(0xFF4A148C),
    secondary = Color(0xFFF8BBD0),
    secondaryContainer = Color(0xFFC2185B),
    background = Color(0xFF000000),
    surface = Color(0xFF180E11),
    surfaceVariant = Color(0xFF25151A),
    error = Color(0xFFEF9A9A)
)

// 7. Charcoal Minimal Scheme
private val CharcoalLightColorScheme = lightColorScheme(
    primary = Color(0xFF37474F),
    primaryContainer = Color(0xFFCFD8DC),
    secondary = Color(0xFF78909C),
    secondaryContainer = Color(0xFFECEFF1),
    background = Color(0xFFECEFF1),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFECEFF1),
    error = Color(0xFFD32F2F)
)
private val CharcoalDarkColorScheme = darkColorScheme(
    primary = Color(0xFFCFD8DC),
    primaryContainer = Color(0xFF21272A),
    secondary = Color(0xFF90A4AE),
    secondaryContainer = Color(0xFF455A64),
    background = Color(0xFF000000),
    surface = Color(0xFF101416),
    surfaceVariant = Color(0xFF191F22),
    error = Color(0xFFEF9A9A)
)

// 8. Sakura Blossom Scheme
private val SakuraLightColorScheme = lightColorScheme(
    primary = Color(0xFFEC407A),
    primaryContainer = Color(0xFFFCE4EC),
    secondary = Color(0xFFF06292),
    secondaryContainer = Color(0xFFFFF0F5),
    background = Color(0xFFFFF0F5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFF0F5),
    error = Color(0xFFD32F2F)
)
private val SakuraDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF48FB1),
    primaryContainer = Color(0xFF880E4F),
    secondary = Color(0xFFFCE4EC),
    secondaryContainer = Color(0xFFAD1457),
    background = Color(0xFF000000),
    surface = Color(0xFF180E11),
    surfaceVariant = Color(0xFF2C1D21),
    error = Color(0xFFEF9A9A)
)

// 9. Midnight Navy Scheme
private val MidnightLightColorScheme = lightColorScheme(
    primary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF1976D2),
    secondaryContainer = Color(0xFFE3F2FD),
    background = Color(0xFFE3F2FD),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE3F2FD),
    error = Color(0xFFD32F2F)
)
private val MidnightDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    primaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFFBBDEFB),
    secondaryContainer = Color(0xFF1976D2),
    background = Color(0xFF000000),
    surface = Color(0xFF0B141C),
    surfaceVariant = Color(0xFF101E2A),
    error = Color(0xFFEF9A9A)
)

@Composable
fun KangaiShopTheme(
    theme: AppTheme = AppTheme.DYNAMIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when (theme) {
        AppTheme.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) BrandDarkColorScheme else BrandLightColorScheme
            }
        }
        AppTheme.BRAND_BLUE -> if (darkTheme) BrandDarkColorScheme else BrandLightColorScheme
        AppTheme.FOREST_GREEN -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
        AppTheme.SUNSET_GOLD -> if (darkTheme) SunsetDarkColorScheme else SunsetLightColorScheme
        AppTheme.OCEAN_BREEZE -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        AppTheme.LAVENDER_FIELD -> if (darkTheme) LavenderDarkColorScheme else LavenderLightColorScheme
        AppTheme.CRIMSON_VELVET -> if (darkTheme) CrimsonDarkColorScheme else CrimsonLightColorScheme
        AppTheme.CHARCOAL_MINIMAL -> if (darkTheme) CharcoalDarkColorScheme else CharcoalLightColorScheme
        AppTheme.SAKURA_BLOSSOM -> if (darkTheme) SakuraDarkColorScheme else SakuraLightColorScheme
        AppTheme.MIDNIGHT_NAVY -> if (darkTheme) MidnightDarkColorScheme else MidnightLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KangaiTypography,
        shapes = KangaiShapes,
        content = content
    )
}
