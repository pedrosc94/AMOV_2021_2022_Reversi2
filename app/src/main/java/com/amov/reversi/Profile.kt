package com.amov.reversi

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class Profile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(com.amov.reversi.TAG, "Profile.onPause()")

        // Draw activity_profile.xml
        setContentView(R.layout.activity_profile)

        // Buttons things
        val profileImg : ImageView = findViewById(
            R.id.profile_profile_image)
        profileImg.setOnClickListener() {
            if (!checkPermissions(this)) {
                requestPermissions(this)
            }
            else {
                Log.d(TAG, "Profile.launchCam()")
            }
        }

        val profileName : TextInputEditText = findViewById(R.id.profile_name_text)
        profileName.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            // Handles the submit button on the keyboard
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // This was added to hide the keyboard, before the keyboard would stay on screen
                val inputMethodManager: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.applicationWindowToken, 0)
                // Clear focus from the textbox
                profileName.clearFocus()
                handled = true
            }
            handled
        }

        val exitBtn : Button = findViewById(R.id.profile_exit_btn)
        exitBtn.setOnClickListener() {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Profile.onPause()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Profile.onResume()")
    }
}