package com.venice.ffaim

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var knob: TextView
    private lateinit var params: WindowManager.LayoutParams
    private var downX = 0f
    private var downY = 0f
    private var startX = 0
    private var startY = 0
    private var moved = false

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val size = (56 * resources.displayMetrics.density).toInt()

        knob = TextView(this).apply {
            text = "FF"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 14f
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xCC1565C0.toInt())
            }
        }

        params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 500
        }

        knob.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    startX = params.x
                    startY = params.y
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - downX).toInt()
                    val dy = (e.rawY - downY).toInt()
                    if (Math.abs(dx) > 12 || Math.abs(dy) > 12) moved = true
                    params.x = startX + dx
                    params.y = startY + dy
                    wm.updateViewLayout(knob, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (moved) {
                        getSharedPreferences("ff", MODE_PRIVATE).edit()
                            .putInt("ax", params.x + size / 2)
                            .putInt("ay", params.y + size / 2)
                            .apply()
                    } else {
                        Bus.flick?.invoke()
                    }
                    true
                }
                else -> false
            }
        }

        wm.addView(knob, params)
    }

    private fun buildNotification(): Notification {
        val ch = "ff"
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(ch, "FF Flick", NotificationManager.IMPORTANCE_LOW))
        }
        val b = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, ch)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return b.setContentTitle("FF Flick активен")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    override fun onDestroy() {
        if (::knob.isInitialized) {
            try { wm.removeView(knob) } catch (_: Exception) {}
        }
        super.onDestroy()
    }
}
