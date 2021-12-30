package com.amov.reversi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.MainThread
import kotlin.system.exitProcess

class Home : AppCompatActivity() {

    private val TAG : String = "LOG"
    private lateinit var btnLocal : Button
    private lateinit var btnOnline : Button
    private lateinit var btnSettings : Button
    private lateinit var btnAbout : Button
    private lateinit var btnExit : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"Home.onCreate()")

        // Draw activity_home.xml
        setContentView(R.layout.activity_home)

        // Buttons things
        btnLocal = findViewById<Button>(R.id.home_btn_local)
        btnLocal.setOnClickListener() {
            val intent = Intent(this@Home, GameLocal::class.java)
            startActivity(intent)
        }
        btnOnline = findViewById<Button>(R.id.home_btn_online)
        btnOnline.setOnClickListener() {
            val intent = Intent(this@Home, GameOnline::class.java)
            startActivity(intent)
        }
        btnSettings = findViewById<Button>(R.id.home_btn_settings)
        btnSettings.setOnClickListener() {
            val intent = Intent(this@Home, Settings::class.java)
            startActivity(intent)
        }
        btnAbout = findViewById<Button>(R.id.home_btn_about)
        btnAbout.setOnClickListener() {
            val intent = Intent(this@Home, About::class.java)
            startActivity(intent)
        }
    }
}