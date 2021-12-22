package com.amov.reversi

import android.Manifest.permission.*
import android.app.Activity
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.nio.file.Files.createFile
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    private val PERMISSION_REQUEST_CODE: Int = 101
    private var mCurrentPhotoPath: String? = null
    lateinit var imageView: ImageView
    lateinit var captureButton: Button

    val TAG : String = "LOG"

    //--------------------------------------------------------------
    // onCreate()
    //--------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"onCreate()")
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.camera_image_preview)
        captureButton = findViewById(R.id.camera_button_capture)

        Log.d(TAG,"Checking permissions...")
        if (hasPermissions()){
            Log.d(TAG,"App has all the permissions!")
        }
        else {
            Log.d(TAG,"Requesting permissions...")
            requestPermissions()
        }
    }

    //--------------------------------------------------------------
    // onResume()
    //--------------------------------------------------------------
    fun onResume(savedInstanceState: Bundle?) {
        super.onResume()
        Log.d(TAG,"onResume()")
    }

    //--------------------------------------------------------------
    // onRequestPermissionsResult()
    //--------------------------------------------------------------
    /*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        Log.d(TAG,"onRequestPermissionsResult()")

        when (requestCode) { PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    takePicture()
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> { }
        }
    }
    */

    //--------------------------------------------------------------
    // onActivityResult()
    //--------------------------------------------------------------
    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG,"onActivityResult")

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //To get the File for further usage
            val auxFile = File(mCurrentPhotoPath)

            var bitmap: Bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
            imageView.setImageBitmap(bitmap)
        }
    }
    */

    //--------------------------------------------------------------
    // Functions
    //--------------------------------------------------------------
    // Permissions
    private fun hasPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                )
    }
    private fun requestPermissions() {
        return ActivityCompat.requestPermissions(this, arrayOf(CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
    }

    // Camera
    /*
    private fun takePicture() {
        val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val file: File = createFile()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "com.example.android.fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }*/

    // Firestore Storage
    /*
    fun createFile() {
        //Firebase STORAGE
        val storage = FirebaseStorage.getInstance()
        // Create a storage reference from our app
        val storageRef = storage.reference

        // WRITE
        // Create a reference to "mountains.jpg"
        val file = storageRef.child("smile.jpg")
    }
    */

    // Utils
    private fun getDateTime() : String {
            val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss.SSS")
            return sdf.format(Date())
    }

    // Testing
    fun usersTEST(){
        // Firebase USERS
        //--------------------------------------------------------------
        val db = Firebase.firestore

        // Create user #1
        // Create a new user with a first and last name
        var user = hashMapOf(
            "date" to getDateTime(),
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )
        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

        // Create user #2
        // Create a new user with a first, middle, and last name
        user = hashMapOf(
            "date" to getDateTime(),
            "first" to "Alan",
            "middle" to "Mathison",
            "last" to "Turing",
            "born" to 1912
        )
        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

        // Read Firebase
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
    fun storageTEST() {
        //Firebase STORAGE
        var storage = FirebaseStorage.getInstance()
        // Create a storage reference from our app
        var storageRef = storage.reference

        // WRITE
        // Create a reference to "mountains.jpg"
        val mountainsRef = storageRef.child("mountains.jpg")
        // Create a reference to 'images/mountains.jpg'
        val mountainImagesRef = storageRef.child("images/mountains.jpg")
        // While the file names are the same, the references point to different files
        mountainsRef.name == mountainImagesRef.name // true
        mountainsRef.path == mountainImagesRef.path // false

        // READ
        // Create a reference with an initial file path and name
        val pathReference = storageRef.child("images/stars.jpg")
        // Create a reference to a file from a Google Cloud Storage URI
        val gsReference = storage.getReferenceFromUrl("gs://bucket/images/stars.jpg")
        // Create a reference from an HTTPS URL
        // Note that in the URL, characters are URL escaped!
        val httpsReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/b/bucket/o/images%20stars.jpg")
    }
    //--------------------------------------------------------------
}