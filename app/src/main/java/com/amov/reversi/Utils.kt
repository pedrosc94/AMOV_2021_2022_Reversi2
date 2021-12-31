package com.amov.reversi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaDrm
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


// Firebase // to remove
private val db = Firebase.firestore

// Logs
val TAG : String = "LOG"

//--------------------------------------------------------------
// Permissions
//--------------------------------------------------------------
val PERMISSION_REQUEST_CODE: Int = 101

fun checkPermissions(context: Context) : Boolean {
    Log.d(TAG,"Checking permissions...")
    return if (hasPermissions(context)) {
        Log.d(TAG,"App has all the permissions!")
        true
    } else {
        Log.d(TAG,"App doesn't have all the permissions!")
        false
    }
}
fun hasPermissions(context : Context): Boolean {
    return (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED                    &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED     &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
}
fun requestPermissions(context : Context) {
    Log.d(TAG,"Requesting permissions...")
    return ActivityCompat.requestPermissions(
        context as Activity, arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ), PERMISSION_REQUEST_CODE)
}

//--------------------------------------------------------------
// Language Change
//--------------------------------------------------------------
 fun selectLang(context: Context, language: String) : String {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.createConfigurationContext(config)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    return language
}

//--------------------------------------------------------------
// DATE
//--------------------------------------------------------------
@SuppressLint("SimpleDateFormat")
fun getDateTime() : String {
    val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss.SSS")
    return sdf.format(Date())
}

//--------------------------------------------------------------
// Generating a Unique ID
//--------------------------------------------------------------
fun byteArrayToHex(byteArray: ByteArray): String {
    var result : String = ""
    for (b in byteArray) {
        val st = String.format("%02X", b)
        result += st
    }
    return result
}

fun getUniqueID() : String {
    val wideVineUuid : UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    return try {
        val wvDrm : MediaDrm = MediaDrm(wideVineUuid);
        val wideVineId : ByteArray = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);

        val s : String = byteArrayToHex(wideVineId);
        s;
    } catch (e : Exception) {
        "UUID=ERROR"
    }
    // Close resources with close() or release() depending on platform API
    // Use ARM on Android P platform or higher, where MediaDrm has the close() method
}

//----------------------------------------------------------
// POSSIBLE STARTUP CODE
//----------------------------------------------------------
/*
// Going to check if app was granted permissions!
if(!checkPermissions()) {
    requestPermissions()
}
// If theres no previous profile goes to Activity -> Profile
else if (!checkExistingProfile()) {
    Log.d(TAG, "Switching to Profile...")
    val intent = Intent(this@Permissions, Profile::class.java)
    startActivity(intent)
}
// If all conditions are met goes to Activity -> Home
else {
    Log.d(TAG, "Switching to Home...")
    val intent = Intent(this@Permissions, Home::class.java)
    startActivity(intent)
}
*/

//----------------------------------------------------------
// Testing FIREBASE
//----------------------------------------------------------
fun usersTEST(){
    // Firebase USERS
    //--------------------------------------------------------------

    // Create a new user with a first and last name
    val user = hashMapOf(
        "date"  to  getDateTime(),
        "first" to  "Pedro",
        "last"  to  "TEST",
        "born"  to  1994
    )
    // Add a new document with a generated ID
    db.collection("users").document(getUniqueID()).set(user)
}
fun storageTEST() {
    //Firebase STORAGE
    val storage = FirebaseStorage.getInstance()
    // Create a storage reference from our app
    val storageRef = storage.reference

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
fun storageTEST2() {
    //Firebase STORAGE
    val storage = FirebaseStorage.getInstance()
    // Create a storage reference from our app
    val storageRef = storage.reference

    // WRITE
    // Create a reference to "mountains.jpg"
    val file = storageRef.child("smile.jpg")
}

// Firebase Profile && Local Profile
//--------------------------------------------------------------
private fun checkExistingProfile() : Boolean {
    var accountExists : Boolean = false
    /*
    db.collection("users")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                if (document.id == getUniqueID()) {
                    Log.d(TAG, "UUID: ${document.id} => ${document.data}")
                    accountExists = true
                }
            }
        }
    */

    db.collection("users").document(getUniqueID()).get().addOnSuccessListener {
        if (it.exists()) accountExists = true
    }

    // TEMP
    return true
}
/*
private fun registerPhone() : Boolean {
    return if(checkExistingProfile()) {
        Toast.makeText(this, "You already have an account!", Toast.LENGTH_SHORT).show();
        false
    } else {
        Toast.makeText(this, "You have no account please create one!", Toast.LENGTH_SHORT).show();
        true
    }
}*/

//----------------------------------------------------------
//----------------------------------------------------------

//----------------------------------------------------------
// DEFAULT CODE FOR FIREBASE DOCUMENTS
//----------------------------------------------------------
/*
// Create user
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
 */