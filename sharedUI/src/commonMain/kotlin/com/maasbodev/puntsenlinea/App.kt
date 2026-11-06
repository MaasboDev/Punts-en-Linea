package com.maasbodev.puntsenlinea

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maasbodev.puntsenlinea.telemetry.data.TelemetryRegistry
import com.maasbodev.puntsenlinea.telemetry.domain.TelemetryCoordinator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

import puntsenlinea.sharedui.generated.resources.Res
import puntsenlinea.sharedui.generated.resources.brand_logo_content_description
import puntsenlinea.sharedui.generated.resources.greeting_prefix
import puntsenlinea.sharedui.generated.resources.ic_logo_dark
import puntsenlinea.sharedui.generated.resources.ic_logo_light
import puntsenlinea.sharedui.generated.resources.toggle_content_button

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB57850),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5DFD0),
    onPrimaryContainer = Color(0xFF3B1E08),
    secondary = Color(0xFF8C583A),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF9EB928),
    background = Color(0xFFF4F5F7),
    onBackground = Color(0xFF241E1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF241E1A),
    surfaceVariant = Color(0xFFEAECEF),
    onSurfaceVariant = Color(0xFF6E5A4F),
    outline = Color(0xFFD5D8DC),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE5A982),
    onPrimary = Color(0xFF44230D),
    primaryContainer = Color(0xFF60371B),
    onPrimaryContainer = Color(0xFFF5DFD0),
    secondary = Color(0xFFD7BCA8),
    onSecondary = Color(0xFF3E2414),
    tertiary = Color(0xFFB8D443),
    background = Color(0xFF181412),
    onBackground = Color(0xFFEDE4DF),
    surface = Color(0xFF241F1C),
    onSurface = Color(0xFFEDE4DF),
    surfaceVariant = Color(0xFF322B27),
    onSurfaceVariant = Color(0xFFCFC3BC),
    outline = Color(0xFF4D443E),
)

internal const val MAIN_SCREEN_NAME = "main_screen"
internal const val MAIN_SCREEN_CLASS = "App"

internal fun onMainScreenDisplayed(telemetry: TelemetryCoordinator) {
    telemetry.logScreenView(
        screenName = MAIN_SCREEN_NAME,
        screenClass = MAIN_SCREEN_CLASS
    )
}

@Composable
@Preview
fun App(
    telemetry: TelemetryCoordinator = TelemetryRegistry.coordinator
) {
    LaunchedEffect(telemetry) {
        onMainScreenDisplayed(telemetry)
    }

    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    ) {
        var showContent by remember { mutableStateOf(true) }
        val logoRes = if (isDark) Res.drawable.ic_logo_dark else Res.drawable.ic_logo_light
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text(text = stringResource(Res.string.toggle_content_button))
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(logoRes),
                        contentDescription = stringResource(Res.string.brand_logo_content_description),
                        modifier = Modifier.size(200.dp),
                    )
                    Text(
                        text = stringResource(Res.string.greeting_prefix, greeting),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}
