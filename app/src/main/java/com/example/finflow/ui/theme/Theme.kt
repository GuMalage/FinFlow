package com.example.finflow.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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

val ProfessionalShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun GerenciadorContasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ProfessionalColorScheme,
        typography = Typography,
        shapes = ProfessionalShapes,
        content = content
    )
}