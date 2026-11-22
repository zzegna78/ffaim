package com.venice.ffaim

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

object Bus {
    var flick: (() -> Unit)? = null
}

class AimService : AccessibilityService() {

    override fun onServiceConnected() {
        Bus.flick = { doFlick() }
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        Bus.flick = null
        super.onDestroy()
    }

    private fun doFlick() {
        val p = getSharedPreferences("ff", MODE_PRIVATE)
        val px = p.getInt("flick", 14).toFloat()
        val ms = p.getInt("ms", 14).toLong().coerceAtLeast(1L)

        val dm = resources.displayMetrics
        val ax = p.getInt("ax", 0).toFloat().takeIf { it > 0f } ?: (dm.widthPixels * 0.62f)
        val ay = p.getInt("ay", 0).toFloat().takeIf { it > 0f } ?: (dm.heightPixels * 0.45f)

        val path = Path().apply {
            moveTo(ax, ay)
            lineTo(ax, ay - px)
        }

        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, ms))
            .build()

        dispatchGesture(g, null, null)
    }
}
