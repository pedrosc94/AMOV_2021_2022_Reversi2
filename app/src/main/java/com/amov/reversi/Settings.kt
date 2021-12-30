package com.amov.reversi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //Buttons things
        val profileBtn : Button = findViewById(R.id.settings_profile_btn)
        profileBtn.setOnClickListener() {
            val intent = Intent(this@Settings, Profile::class.java)
            startActivity(intent)
        }
        val langBtn : Button = findViewById(R.id.settings_lang_btn)
        langBtn.setOnClickListener() {

        }
        val exitBtn : Button = findViewById(R.id.settings_exit_btn)
        exitBtn.setOnClickListener() {
            finish()
        }
    }
}