package com.sacredpath.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sacredpath.app.ui.navigation.SacredPathNavHost
import com.sacredpath.app.ui.theme.SacredPathTheme
import com.sacredpath.app.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen must be installed before super.onCreate
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsRepository.settingsFlow.collectAsState(initial = null)

            // Keep splash visible until settings load
            splash.setKeepOnScreenCondition { settings == null }

            settings?.let { prefs ->
                SacredPathTheme(themeMode = prefs.themeMode) {
                    SacredPathNavHost(
                        startDestination = if (prefs.hasCompletedOnboarding)
                            "main" else "onboarding"
                    )
                }
            }
        }
    }
}
