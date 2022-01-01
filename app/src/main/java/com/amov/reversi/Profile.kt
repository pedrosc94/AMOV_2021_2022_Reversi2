package com.amov.reversi

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Profile : AppCompatActivity() {

    private val db = Firebase.firestore
    private val CAPTURE_CODE = 1
    private lateinit var profileImg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(com.amov.reversi.TAG, "Profile.onPause()")

        // Draw activity_profile.xml
        setContentView(R.layout.activity_profile)

        // Profile Image
        profileImg = findViewById(R.id.profile_profile_image)
        profileImg.setOnClickListener() {
            if (!checkPermissions(this)) {
                requestPermissions(this)
            }
            else {
                Log.d(TAG, "Profile.launchCam()")
                val intent : Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent,CAPTURE_CODE)
            }
        }

        // Profile Name
        val profileName : TextInputEditText = findViewById(R.id.profile_name_text)
        profileName.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            // Handles the submit button on the keyboard
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // This was added to hide the keyboard, before the keyboard would stay on screen
                val inputMethodManager: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.applicationWindowToken, 0)
                // Clear focus from the text box
                profileName.clearFocus()
                handled = true
            }
            // Updating firebase & local with new username
            // TODO db.collection("users").document(getUniqueID()).get()........ name = profileName.text
            Log.d(TAG, "Updated the username!")
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

    // Using this just for camera quick solution
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_CODE) run {
            val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
            // Encoding to String to save in same document where all the other user info is stored
            val bitmapEncoded64 : String = encodeToBase64(bitmap)
            // Updating firebase & local with the new picture
            // TODO db.collection("users").document(getUniqueID()).get()........ img = bitmapEncoded64
            // Just testing the decoding
            val bitmapDecoded64 : Bitmap = decodeBase64(bitmapEncoded64)
            profileImg.setImageBitmap(bitmapDecoded64)
        }
    }
}