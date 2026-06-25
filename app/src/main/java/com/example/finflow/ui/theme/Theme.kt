package com.example.finflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ProfessionalColorScheme = lightColorScheme(
    primary = AzulNaval,
    secondary = AzulSuve,
    background = FundoFrio,
    surface = CardBranco,
    onPrimary = CardBranco,
    onBackground = TextoPrincipal,
    onSurface = TextoPrincipal,
    outline = TextoSecundario
)

private val DarkProfessionalColorScheme = darkColorScheme(
    primary = AzulSuve,
    secondary = AzulNaval,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    outline = Color(0xFF64748B)
)

val ProfessionalShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun GerenciadorContasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkProfessionalColorScheme else ProfessionalColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ProfessionalShapes,
        content = content
    )
}