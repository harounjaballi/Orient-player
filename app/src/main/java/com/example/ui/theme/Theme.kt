package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class OrientThemeStyle(val label: String) {
    MIDNIGHT_BLUE("Bleu Nuit & Noir Haute-Contraste (Défaut)"),
    AMOLED_BLACK("Noir Absolu AMOLED & Turquoise"),
    CYBER_TURQUOISE("Océan Électrique & Cyan")
}

object OrientGradients {
    val PrimaryButtonBrush: Brush = Brush.horizontalGradient(
        colors = listOf(OrientElectricBlue, OrientTurquoise)
    )

    val ActiveNavigationBrush: Brush = Brush.linearGradient(
        colors = listOf(OrientElectricBlue, Color(0xFF00A8FF), OrientTurquoise)
    )

    val ActiveBorderBrush: Brush = Brush.linearGradient(
        colors = listOf(OrientTurquoise, OrientElectricBlueLight, OrientTurquoiseGlow)
    )

    val BackgroundVerticalBrush: Brush = Brush.verticalGradient(
        colors = listOf(OrientMidnightDark, OrientDeepNavy, OrientMidnightDark)
    )

    val CardActiveSurfaceBrush: Brush = Brush.horizontalGradient(
        colors = listOf(OrientCardNavy, Color(0xFF0B2948))
    )
}

private val MidnightBlueHighContrastScheme = darkColorScheme(
    primary = OrientElectricBlue,
    onPrimary = Color.White,
    primaryContainer = OrientCardNavy,
    onPrimaryContainer = OrientTurquoise,
    secondary = OrientTurquoise,
    onSecondary = OrientMidnightDark,
    secondaryContainer = Color(0xFF00364D),
    onSecondaryContainer = OrientTurquoiseGlow,
    tertiary = OrientGold,
    onTertiary = OrientMidnightDark,
    background = OrientMidnightDark,
    onBackground = OrientTextPrimary,
    surface = OrientDeepNavy,
    onSurface = OrientTextPrimary,
    surfaceVariant = OrientSurfaceNavy,
    onSurfaceVariant = OrientTextSecondary,
    outline = OrientBorderBlue,
    outlineVariant = OrientElectricBlueLight,
    error = OrientCoralRed,
    onError = Color.White
)

private val AmoledHighContrastScheme = MidnightBlueHighContrastScheme.copy(
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = Color(0xFF0B1528),
    primary = OrientTurquoise,
    onPrimary = AmoledBackground,
    secondary = OrientElectricBlue
)

private val CyberTurquoiseHighContrastScheme = MidnightBlueHighContrastScheme.copy(
    primary = OrientTurquoise,
    onPrimary = OrientMidnightDark,
    secondary = OrientElectricBlueLight,
    background = CyberBackground,
    surface = CyberSurface,
    surfaceVariant = Color(0xFF0D2B42)
)

val OrientShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun OrientPlayerTheme(
    themeStyle: OrientThemeStyle = OrientThemeStyle.MIDNIGHT_BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeStyle) {
        OrientThemeStyle.MIDNIGHT_BLUE -> MidnightBlueHighContrastScheme
        OrientThemeStyle.AMOLED_BLACK -> AmoledHighContrastScheme
        OrientThemeStyle.CYBER_TURQUOISE -> CyberTurquoiseHighContrastScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OrientShapes,
        content = content
    )
}

/**
 * Reusable high-contrast Electric Blue -> Turquoise gradient button for primary actions and active states.
 */
@Composable
fun OrientGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(shape)
            .background(OrientGradients.PrimaryButtonBrush)
            .border(1.dp, OrientTurquoiseGlow.copy(alpha = 0.65f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun OrientGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
) {
    OrientGradientButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Reusable high-contrast navigation / filter pill with Electric Blue -> Turquoise gradient when active.
 */
@Composable
fun OrientGradientPill(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .background(OrientGradients.ActiveNavigationBrush)
                        .border(1.2.dp, OrientTurquoiseGlow, shape)
                } else {
                    Modifier
                        .background(OrientSurfaceNavy)
                        .border(1.dp, OrientBorderBlue, shape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
fun OrientGradientPill(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    OrientGradientPill(
        selected = selected,
        onClick = onClick,
        modifier = modifier
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (selected) Color.White else OrientTurquoise,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else OrientTextPrimary
        )
    }
}
