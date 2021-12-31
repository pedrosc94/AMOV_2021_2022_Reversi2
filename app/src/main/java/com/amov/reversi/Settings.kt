package com.amov.reversi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import java.util.*

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Shared Preferences
        val PREFS_NAME = BuildConfig.APPLICATION_ID + ".PREFS"
        val PREFS_LANG = "SELECTED_LANG"
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val language : String? = prefs.getString(PREFS_LANG,"null")
        if (language != null) {
            selectLang(this,language)
        }

        super.onCreate(savedInstanceState)
        Log.d(com.amov.reversi.TAG, "Settings.onCreate()")

        // Draw activity_settings.xml
        setContentView(R.layout.activity_settings)

        // Profile
        val profileBtn : Button = findViewById(R.id.settings_profile_btn)
        profileBtn.setOnClickListener() {
            val intent = Intent(this@Settings, Profile::class.java)
            startActivity(intent)
        }

        // Language
        // KINDA WORKS, CAN BE IMPROVED
        val langBtn : Button = findViewById(R.id.settings_lang_btn)
        langBtn.setOnClickListener() {
            if (prefs.getString(PREFS_LANG,"null") == "en") {
                val lang : String = selectLang(this,"pt")
                prefs.edit().putString(PREFS_LANG,lang).apply()
            }
            else {
                val lang : String = selectLang(this,"en")
                prefs.edit().putString(PREFS_LANG,lang).apply()
            }
            finish()
            startActivity(intent)
        }

        // Exit
        val exitBtn : Button = findViewById(R.id.settings_exit_btn)
        exitBtn.setOnClickListener() {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(com.amov.reversi.TAG, "Settings.onPause()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(com.amov.reversi.TAG, "Settings.onResume()")
    }
}