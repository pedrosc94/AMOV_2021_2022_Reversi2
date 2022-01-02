package com.amov.reversi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaDrm
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

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
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED    &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
            )
}
fun requestPermissions(context : Context) {
    Log.d(TAG,"Requesting permissions...")
    return ActivityCompat.requestPermissions(
        context as Activity, arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
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
// Encoding & Decoding (camera bitmap)
//--------------------------------------------------------------
fun encodeToBase64(image: Bitmap): String {
    val baos = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val b: ByteArray = baos.toByteArray()
    val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
    Log.d(TAG, "Image was encoded!")
    return imageEncoded
}

fun decodeBase64(input: String?): Bitmap {
    val decodedByte = Base64.decode(input, 0)
    Log.d(TAG, "Image was decoded!")
    return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
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

//--------------------------------------------------------------
// Documents - Firebase
//--------------------------------------------------------------
fun createEmptyUser(){
    val db = Firebase.firestore

    val user = hashMapOf(
        "username" to  null,
        "created"  to  getDateTime(),
        "image"    to  null
    )
    if(getUniqueID().length <= 1) {}
    else {
        // Add a new document with a ID that is linked to device!
        db.collection("users").document(getUniqueID()).set(user)
    }
    Log.d(TAG,user["username"].toString())
}

/* NOT WORKING WELL
fun findUserInfo(id : String, field : String): String {
    val db = Firebase.firestore

    var r : String = ""

    db.collection("users")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                if (document.id == id) {
                    r = document.data.get("username").toString()
                    Log.d(TAG, "${document.id} => ${document.data.get("username")}")
                }
            }
        }
        .addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents.", exception)
        }
    Thread.sleep(1000);

    return r
}*/

//--------------------------------------------------------------
// Storage - Firebase
//--------------------------------------------------------------
fun uploadImageToStorage(bitmap: Bitmap) {
    // Create a storage reference from our app
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    // Create a reference to "mountains.jpg"
    val fileImage = storageRef.child(getUniqueID() + ".jpg")

    /*
    // Create a reference to 'images/mountains.jpg'
    val fileImageRef = storageRef.child("images/" + getUniqueID() + ".jpg")
    // While the file names are the same, the references point to different files
    fileImage.name == fileImageRef.name // true
    fileImage.path == fileImageRef.path // false
    */

    // Output Stream
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()

    // Firestore Upload
    var uploadTask = fileImage.putBytes(data)
    uploadTask.addOnFailureListener {
        Log.d(TAG, "Upload failed!")
    }.addOnSuccessListener { taskSnapshot ->
        Log.d(TAG, "Upload successful!")
    }
}

/*
fun downloadImageFromStorage(id : String) : Bitmap {
    var bmp : Bitmap? = null

    return bmp
}*/

//
// Clipboard
//
fun copyMyID(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", getUniqueID())
    clipboard.setPrimaryClip(clip)
}

//----------------------------------------------------------
// DialogBox
//----------------------------------------------------------
fun showDialogMsg(title: String?, activity: Activity, context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setCancelable(false);

    if(title == "Language") {
        builder.setTitle("Select Language")
        builder.setMessage("Selecionar Linguagem")
        builder.setPositiveButton(
            "Português"
        ) { dialog, which ->
            selectLang(context, "pt")
            activity.finish()
            activity.startActivity(Intent(activity.intent))
        }
        builder.setNegativeButton("English") { dialog, which ->
            selectLang(context,"en")
            activity.finish()
            activity.startActivity(Intent(activity.intent))
        }.show()
    }
    else {
        builder.setTitle(R.string.game_end_title)
        builder.setMessage(R.string.game_end_msg)
        builder.setPositiveButton(
            "OK"
        ) { dialog, which ->
            activity.finish()
        }.show()
    }
}

//----------------------------------------------------------
//
//
// CODE IDEAS/TEST
//
//
//----------------------------------------------------------

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
/*
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
fun checkExistingProfile() : Boolean {
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
*/

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