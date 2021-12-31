package com.amov.reversi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaDrm
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
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