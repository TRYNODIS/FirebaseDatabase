package hu.nagyi.sixthweekwork

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.nagyi.sixthweekwork.data.Advertisement
import hu.nagyi.sixthweekwork.databinding.ActivityCreateAdvertisementBinding
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*

class CreateAdvertisementActivity : AppCompatActivity() {

    //region VARIABLES

    lateinit var binding: ActivityCreateAdvertisementBinding

    var uploadBitmap: Bitmap? = null

    //endregion

    //region METHODS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityCreateAdvertisementBinding.inflate(this.layoutInflater)
        this.setContentView(this.binding.root)

        this.requestNeededPermission()
    }

    fun sendClick(v: View) {
        if (this.uploadBitmap == null) {
            this.advertisementUpload()
        } else {
            try {
                this.imageWithAdvertisementUpload()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun attachClick(v: View) {
        this.startActivityForResult(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE),
            Constants.CAMERA_REQUEST_CODE
        )
    }

    private fun advertisementUpload(imageUrl: String = "") {
        val advertisement = Advertisement(
            FirebaseAuth.getInstance().currentUser!!.uid,
            FirebaseAuth.getInstance().currentUser!!.email!!,
            this.binding.titleET.text.toString(),
            this.binding.descriptionET.text.toString(),
            this.binding.priceET.text.toString(),
            this.binding.emailET.text.toString(),
            this.binding.phoneET.text.toString(),
            imageUrl
        )

        val advertisementsCollection = FirebaseFirestore.getInstance().collection(
            Constants.COLLECTION_ADVERTISEMENTS
        )

        advertisementsCollection.add(advertisement)
            .addOnSuccessListener {
                Toast.makeText(
                    this@CreateAdvertisementActivity,
                    "Advertisement SAVED", Toast.LENGTH_LONG
                ).show()

                this.finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@CreateAdvertisementActivity,
                    "Error ${it.message}", Toast.LENGTH_LONG
                ).show()
            }
    }

    @Throws(Exception::class)
    private fun imageWithAdvertisementUpload() {
        val baos = ByteArrayOutputStream()
        this.uploadBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().reference
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")

        newImagesRef.putBytes(imageInBytes)
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@CreateAdvertisementActivity,
                    exception.message,
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnSuccessListener {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                newImagesRef.downloadUrl.addOnCompleteListener { task ->
                    this@CreateAdvertisementActivity.advertisementUpload(
                        task.result.toString()
                    )
                }
            }
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.CAMERA
                )
            ) {
                Toast.makeText(
                    this,
                    "I need it for camera", Toast.LENGTH_SHORT
                ).show()
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                Constants.PERMISSION_REQUEST_CODE
            )
        } else {
            // we already have permission
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "CAMERA perm granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "CAMERA perm NOT granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            this.uploadBitmap = data!!.extras!!.get("data") as Bitmap
            this.binding.attachedIV.setImageBitmap(this.uploadBitmap)
            this.binding.attachedIV.visibility = View.VISIBLE
        }
    }

    //endregion

}