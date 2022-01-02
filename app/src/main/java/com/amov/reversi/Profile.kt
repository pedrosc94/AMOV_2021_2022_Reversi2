package com.amov.reversi

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.common.io.BaseEncoding
import com.google.common.primitives.Bytes
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class Profile : AppCompatActivity() {

    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val CAPTURE_CODE = 1
    private lateinit var profileImg: ImageView
    private lateinit var profileName : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(com.amov.reversi.TAG, "Profile.onPause()")

        // Draw activity_profile.xml
        setContentView(R.layout.activity_profile)

        profileImg = findViewById(R.id.profile_profile_image)
        profileName = findViewById(R.id.profile_name_text)

        // Take Photo
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

        // Change Name
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
            db.collection("users").document(getUniqueID()).update("username",profileName.text.toString())
            Log.d(TAG, "Updated the username!")
            //profileName.setText(findUserInfo(getUniqueID(),"username"))
            handled
        }

        // Share Button
        val shareBtn : Button = findViewById(R.id.profile_share_btn)
        shareBtn.setOnClickListener() {
            copyMyID(this)
        }

        // Exit button
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
        updateName()
    }

    // Using this just for camera quick solution
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_CODE) run {
            val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
            uploadImageToStorage(bitmap)


            /*
            // Encoding to String to save in same document where all the other user info is stored
            val bitmapEncoded64: String = encodeToBase64(bitmap)
            // Set Image
            db.collection("users").document(getUniqueID()).update("image", bitmapEncodeLayer2)
            // Get Image
            var img: String = ""
            db.collection("users").document(getUniqueID()).get().addOnSuccessListener { document -> img = document.get("image").toString() }
            // Decoding Image
            val bitmapDecoded64: Bitmap = decodeBase64(img)
            */
            findViewById<ImageView>(R.id.profile_profile_image).setImageBitmap(bitmap)
        }
    }

    private fun updateName() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.id == getUniqueID()) {
                        // Updates username text
                        findViewById<TextView>(R.id.profile_name_text).text = document.data.get("username").toString()
                        // Image
                        //findViewById<ImageView>(R.id.profile_profile_image).setImageBitmap(decodeBase64(document.data.get("image").toString()))
                        Log.d(TAG, "${document.id} => ${document.data.get("username")}")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}