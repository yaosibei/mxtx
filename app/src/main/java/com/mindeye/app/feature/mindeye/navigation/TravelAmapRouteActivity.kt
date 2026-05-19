package com.mindeye.app.feature.mindeye.navigation

import android.os.Bundle
import com.amap.api.navi.AmapRouteActivity

class TravelAmapRouteActivity : AmapRouteActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AmapNavigationManager.onInternalRoutePageOpened()
    }

    override fun onDestroy() {
        AmapNavigationManager.onInternalRoutePageClosed()
        super.onDestroy()
    }
}
