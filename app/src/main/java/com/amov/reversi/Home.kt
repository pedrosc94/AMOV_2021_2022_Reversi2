package com.amov.reversi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class Home : AppCompatActivity() {

    private val TAG : String = "LOG"
    private val PREFS_NAME = BuildConfig.APPLICATION_ID + ".PREFS"
    private val PREFS_VERSION_CODE_KEY = BuildConfig.VERSION_NAME
    private val PREFS_LANG = "SELECTED_LANG"
    private val CURR_VERSION_CODE = BuildConfig.VERSION_CODE

    private lateinit var btnLocal : Button
    private lateinit var btnOnline : Button
    private lateinit var btnSettings : Button
    private lateinit var btnAbout : Button

    private lateinit var LANG : String

    override fun onCreate(savedInstanceState: Bundle?) {

        // Checks for previous language config -> this can be moved to function. maybe improved?
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val language : String? = prefs.getString(PREFS_LANG,"null")
        if (language != null) {
            LANG = language
            selectLang(this,language)
        }
        Log.d(com.amov.reversi.TAG, "Language: $language")

        super.onCreate(savedInstanceState)
        Log.d(TAG,"Home.onCreate()")

        // Check if its the first time running
        checkFirstRun()

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

    override fun onPause() {
        super.onPause()
        Log.d(com.amov.reversi.TAG, "Home.onPause()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(com.amov.reversi.TAG, "Home.onResume()")
        // KINDA WORKS, CAN BE IMPROVED
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val language : String? = prefs.getString(PREFS_LANG,"null")
        if (language != null) {
            Log.d(com.amov.reversi.TAG, "Language: $language")
            selectLang(this,language)
        }
        if (LANG != language) {
            finish()
            startActivity(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(com.amov.reversi.TAG, "Home.onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(com.amov.reversi.TAG, "Home.onDestroy()")
    }

    // With help from
    // https://stackoverflow.com/a/30274315
    private fun checkFirstRun() {
        Log.d(TAG, "Checking if its app first time running...")

        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedVersionCode = prefs.getInt(PREFS_VERSION_CODE_KEY, -1) // Default value -1 means that wasn't found any previous version
        // Check for first run or upgrade
        when {
            CURR_VERSION_CODE == savedVersionCode -> {
                Log.d(TAG, "This is a normal run!")
                createEmptyUser()
                return
            }
            // -1 -> Doesn't exist
            savedVersionCode == -1 -> {
                Log.d(TAG, "This is a new install (or the user cleared the shared preferences)!")
                //checkProfile()
            }
            // We wont use this probably
            CURR_VERSION_CODE > savedVersionCode -> {
                Log.d(TAG, "This is a run after an upgrade!")
                return
            }
        }
        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREFS_VERSION_CODE_KEY, CURR_VERSION_CODE).apply()
    }
}