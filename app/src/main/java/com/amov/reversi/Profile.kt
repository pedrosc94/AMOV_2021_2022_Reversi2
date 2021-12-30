package com.amov.reversi

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Profile : AppCompatActivity() {
    // Logs
    private val TAG : String = "LOG"
    // Permissions
    private val PERMISSION_REQUEST_CODE: Int = 101
    // Firebase
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }

    //--------------------------------------------------------------
    // Permissions
    //--------------------------------------------------------------
    private fun checkPermissions() : Boolean {
        Log.d(TAG,"Checking permissions...")
        return if (hasPermissions()) {
            Log.d(TAG,"App has all the permissions!")
            true
        } else {
            Log.d(TAG,"App doesn't have all the permissions!")
            false
        }
    }
    private fun hasPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED                    &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED     &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }
    private fun requestPermissions() {
        Log.d(TAG,"Requesting permissions...")
        return ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), PERMISSION_REQUEST_CODE)
    }
}