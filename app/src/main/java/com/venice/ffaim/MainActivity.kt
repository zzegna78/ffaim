package com.venice.ffaim

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val prefs = getSharedPreferences("ff", MODE_PRIVATE)

        val ll = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(56, 120, 56, 56)
        }

        fun title(t: String) {
            ll.addView(TextView(this).apply { text = t; textSize = 18f; setPadding(0, 0, 0, 24) })
        }

        title("FF Flick Assist")
        ll.addView(TextView(this).apply {
            text = "1. Разреши оверлей\n2. Включи службу FF Flick\n3. Покажи плавающую кнопку\n4. Перетащи кнопку в удобное место, настраивай силу свайпа"
            textSize = 14f
            setPadding(0, 0, 0, 32)
        })

        ll.addView(Button(this).apply {
            text = "Разрешить оверлей"
            setOnClickListener {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        })

        ll.addView(Button(this).apply {
            text = "Включить спец.возможности"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })

        ll.addView(Button(this).apply {
            text = "Показать плавающую кнопку"
            setOnClickListener { startService(Intent(this@MainActivity, OverlayService::class.java)) }
        })

        ll.addView(Button(this).apply {
            text = "Скрыть плавающую кнопку"
            setOnClickListener { stopService(Intent(this@MainActivity, OverlayService::class.java)) }
        })

        val flLabel = TextView(this).apply {
            text = "Сила дёрга: ${prefs.getInt("flick", 14)} px"
            textSize = 14f
            setPadding(0, 40, 0, 0)
        }
        ll.addView(flLabel)
        ll.addView(SeekBar(this).apply {
            max = 59
            progress = prefs.getInt("flick", 14) - 1
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, pr: Int, fromUser: Boolean) {
                    val v = pr + 1
                    flLabel.text = "Сила дёрга: $v px"
                    prefs.edit().putInt("flick", v).apply()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        })

        val msLabel = TextView(this).apply {
            text = "Скорость свайпа: ${prefs.getInt("ms", 14)} мс"
            textSize = 14f
            setPadding(0, 40, 0, 0)
        }
        ll.addView(msLabel)
        ll.addView(SeekBar(this).apply {
            max = 75
            progress = prefs.getInt("ms", 14) - 5
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, pr: Int, fromUser: Boolean) {
                    val v = pr + 5
                    msLabel.text = "Скорость свайпа: $v мс"
                    prefs.edit().putInt("ms", v).apply()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        })

        setContentView(ll)
    }
}
