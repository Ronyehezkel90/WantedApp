package com.ronyehezkel.wantedapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.ronyehezkel.wantedapp.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imagePath: String
    private lateinit var cameraBitmap: Bitmap


    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)

    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    private fun rotateIfNeeded(cameraBitmap: Bitmap): Bitmap {
        val ei = ExifInterface(imagePath)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 ->
                rotateImage(cameraBitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 ->
                rotateImage(cameraBitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 ->
                rotateImage(cameraBitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> cameraBitmap
            else -> cameraBitmap
        }
    }

    // Get your image
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imgFile = File(imagePath)
                if (imgFile.exists()) {
                    cameraBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
//                        cameraBitmap = rotateIfNeeded(cameraBitmap)
                    viewBinding.thumbnailImageView.setImageBitmap(cameraBitmap)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (!allPermissionsGranted()) {
            requestPermissions()
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (Environment.isExternalStorageManager()) {
//                //todo when permission is granted
//            } else {
//                //request for the permission
//                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//                val uri = Uri.fromParts("package", packageName, null)
//                intent.data = uri
//                startActivity(intent)
//            }
//        }
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // Set up the listeners for take photo and video capture buttons
        viewBinding.thumbnailImageView.setOnClickListener {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "$timeStamp.jpg"
//            val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val storageDir: File = externalCacheDir!!
            imagePath = storageDir.absolutePath.toString() + "/" + imageFileName
            val file = File(imagePath)
            val outputFileUri: Uri = Uri.fromFile(file)
            // Open camera
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            resultLauncher.launch(cameraIntent)
        }

        val activity = this
        viewBinding.print.setOnClickListener {
            Thread().run {
                Printer().print(cameraBitmap, activity, viewBinding.thumbnailImageView)
            }
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "WantedApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}