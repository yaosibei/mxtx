package com.mindeye.app.feature.common.ui

import android.content.Context
import android.view.accessibility.AccessibilityManager

fun Context.isTouchExplorationEnabled(): Boolean {
    val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    return manager?.isTouchExplorationEnabled == true
}

