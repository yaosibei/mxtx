package com.mindeye.app.feature.mindeye.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.amap.api.maps.model.LatLng

object TravelAmapExternalFallback {

    fun launch(
        context: Context,
        startName: String,
        startPoint: LatLng,
        destinationName: String,
        destinationPoint: LatLng
    ): Boolean {
        val uri = Uri.parse(
            "androidamap://route?" +
                "sourceApplication=${Uri.encode("明心同行")}" +
                "&slat=${startPoint.latitude}" +
                "&slon=${startPoint.longitude}" +
                "&sname=${Uri.encode(startName)}" +
                "&dlat=${destinationPoint.latitude}" +
                "&dlon=${destinationPoint.longitude}" +
                "&dname=${Uri.encode(destinationName)}" +
                "&dev=0&t=2"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.autonavi.minimap")
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
