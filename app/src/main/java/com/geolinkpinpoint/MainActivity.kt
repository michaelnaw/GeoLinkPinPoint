package com.geolinkpinpoint

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.geolinkpinpoint.ui.MainViewModel
import com.geolinkpinpoint.ui.navigation.AppNavigation
import com.geolinkpinpoint.ui.theme.GeoLinkPinPointTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            GeoLinkPinPointTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data?.toString() ?: return
        if (uri.startsWith("geo:")) {
            viewModel.handleGeoUri(uri)
        }
    }
}
