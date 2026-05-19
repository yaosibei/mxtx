package com.mindeye.app.feature.mindeye.ui

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.core.ServiceSettings

@Composable
fun AmapPreviewMap(
    centerPoint: LatLng,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "高德地图预览",
    onMapStatusChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestStatusCallback by rememberUpdatedState(onMapStatusChanged)
    var mapStatus by remember { mutableStateOf("正在加载高德地图预览。") }

    val mapView = remember {
        MapsInitializer.updatePrivacyShow(context, true, true)
        MapsInitializer.updatePrivacyAgree(context, true)
        ServiceSettings.updatePrivacyShow(context, true, true)
        ServiceSettings.updatePrivacyAgree(context, true)

        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    LaunchedEffect(mapStatus) {
        latestStatusCallback(mapStatus)
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                update = { view ->
                    runCatching {
                        val map = view.map
                        map.setOnMapLoadedListener {
                            mapStatus = "高德地图预览加载完成。"
                        }
                        map.uiSettings.isZoomControlsEnabled = false
                        map.uiSettings.isScaleControlsEnabled = false
                        map.uiSettings.isRotateGesturesEnabled = false
                        map.uiSettings.isTiltGesturesEnabled = false
                        map.clear()
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 16f))
                        map.addMarker(
                            MarkerOptions()
                                .position(centerPoint)
                                .title(title)
                                .snippet(subtitle)
                        )
                    }.onFailure {
                        mapStatus = "高德地图预览加载失败：${it.message ?: "未知错误"}"
                    }
                }
            )
        }
    }
}
