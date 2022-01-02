package com.amov.reversi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Permissions : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"Permissions.onCreate()")

        // Draw activity_permissions.xml
        setContentView(R.layout.activity_permissions)

        // Buttons things
        val requestBtn : Button = findViewById(R.id.permissions_request_btn)
        requestBtn.setOnClickListener() {
            requestPermissions(this)
        }
        val exitBtn : Button = findViewById(R.id.permissions_exit_btn)
        exitBtn.setOnClickListener() {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Permissions.onPause()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Permissions.onResume()")

        if (checkPermissions(this)) {
            val intent = Intent(this@Permissions, Profile::class.java)
            startActivity(intent)
        }
    }
}