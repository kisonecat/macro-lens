package com.macrolens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.macrolens.ui.PhoodApp
import com.macrolens.ui.theme.PhoodTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : ComponentActivity() {

    private val initialRoute = MutableStateFlow<String?>(null)
    private val incomingRoute = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initialRoute.value = intent?.extractRoute()

        setContent {
            PhoodTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhoodApp(
                        startRoute = initialRoute.value,
                        incomingRoutes = incomingRoute.asSharedFlow()
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.extractRoute()?.let { incomingRoute.tryEmit(it) }
    }

    private fun Intent.extractRoute(): String? = getStringExtra(EXTRA_START_ROUTE)

    companion object {
        const val EXTRA_START_ROUTE = "start_route"
        const val ROUTE_DAILY_LOG = "daily_log"
    }
}
