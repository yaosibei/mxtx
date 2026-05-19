package com.mindeye.app.feature.mindeye.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent

object TravelAmapNaviLauncher {

    private const val EXTRA_DESTINATION_NAME = "travel_destination_name"

    fun launch(context: Context, destinationName: String) {
        val intent = Intent(context, TravelAmapNaviActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION_NAME, destinationName)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }

    fun readDestination(intent: Intent): String {
        return intent.getStringExtra(EXTRA_DESTINATION_NAME).orEmpty()
    }
}
