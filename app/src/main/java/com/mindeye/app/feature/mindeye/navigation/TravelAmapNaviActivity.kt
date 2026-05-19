package com.mindeye.app.feature.mindeye.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class TravelAmapNaviActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destinationName = TravelAmapNaviLauncher.readDestination(intent)
        if (destinationName.isBlank()) {
            finish()
            return
        }

        if (savedInstanceState == null) {
            AmapNavigationManager.launchFromActivity(this, destinationName)
        }

        setContent {
            val launchState by AmapNavigationManager.launchState.collectAsStateWithLifecycle()
            LaunchedEffect(launchState.stage) {
                val shouldFinish =
                    launchState.stage == AmapNavigationManager.TravelNaviLaunchStage.INTERNAL_ROUTE_PAGE_OPENED ||
                        launchState.stage == AmapNavigationManager.TravelNaviLaunchStage.INTERNAL_NAVI_STARTED ||
                        launchState.stage == AmapNavigationManager.TravelNaviLaunchStage.EXTERNAL_APP_LAUNCHED ||
                        launchState.stage == AmapNavigationManager.TravelNaviLaunchStage.FAILED
                if (shouldFinish) {
                    finish()
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = launchState.message,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
