package com.amov.reversi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Profile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw activity_profile.xml
        setContentView(R.layout.activity_profile)

        // Buttons things
        val profileImg : ImageView = findViewById(R.id.profile_profile_image)
        profileImg.setOnClickListener() {
            if (!checkPermissions(this)) {
                requestPermissions(this)
            }
            else {
                Log.d(TAG, "Profile.launchCam()")
            }
        }
        val exitBtn : Button = findViewById(R.id.profile_exit_btn)
        exitBtn.setOnClickListener() {
            finish()
        }
    }
}