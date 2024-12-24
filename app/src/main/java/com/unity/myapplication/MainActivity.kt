package com.unity.myapplication

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-12-24 10
 * Time:41
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_scroll)

        findViewById<VerticalScrollTextView>(R.id.tv_view).setText("大兴安岭博物馆")
        findViewById<VerticalScrollTextView>(R.id.tv_view).setTextSize(32f)
        findViewById<VerticalScrollTextView>(R.id.tv_view).setTextColor(Color.parseColor("#FF0000"))

        findViewById<Button>(R.id.bt_start).setOnClickListener {
            findViewById<VerticalScrollTextView>(R.id.tv_view).startScroll()
        }

        findViewById<Button>(R.id.bt_stop).setOnClickListener {
            findViewById<VerticalScrollTextView>(R.id.tv_view).stopScroll()
        }
    }
}