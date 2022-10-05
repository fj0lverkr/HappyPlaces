package com.nilsnahooy.happyplaces

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nilsnahooy.happyplaces.databinding.ActivityAddPlaceBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddPlaceActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener {
    companion object {
        private const val TAG = "HappyPlacesApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private var b: ActivityAddPlaceBinding? = null
    private var cal = Calendar.getInstance()

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private var galleryResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val data: Intent? = result.data
                b?.ivImagePreview?.setImageURI(data?.data)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(b?.root)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        
        dateSetListener = DatePickerDialog.OnDateSetListener {
                _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            formatDateInField()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        b?.tilTitle?.markRequired()
        b?.tilLocation?.markRequired()

        b?.etDate?.setOnClickListener(this)
        b?.tvButtonAddImage?.setOnClickListener(this)
        b?.btnTakePicture?.setOnClickListener{takePicture()}
        b?.tilTitle?.editText?.onFocusChangeListener = this
        b?.tilLocation?.editText?.onFocusChangeListener = this
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //extension function to show fields are required
    private fun TextInputLayout.markRequired() {
        hint = "$hint *"
    }

    private fun validateInput(v: View?){
        val view = v as TextInputEditText
        if(view.text?.isEmpty() == true || view.text == null){
            view.error = getString(R.string.error_required_field)
        } else {
            view.error = null
        }
    }

    private fun formatDateInField(){
        val myFormat = getString(R.string.fmt_date)
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        b?.etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(b?.pvViewfinder?.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))

        b?.clViewFinderWrapper?.visibility = View.VISIBLE
    }

    private fun takePicture(){
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HappyPlaces")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    b?.ivImagePreview?.setImageURI(output.savedUri)
                    b?.clViewFinderWrapper?.visibility = View.INVISIBLE
                }
            }
        )
    }

    private fun setPhoto(src: Int){
        if (allPermissionsGranted()){
            val pickFromGallery = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            when(src){
                0 -> galleryResultLauncher.launch(pickFromGallery)
                1 -> startCamera()
            }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            setPhoto(src)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.et_date -> {
                DatePickerDialog(this,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_button_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                val pictureDialogItems = arrayOf(getString(R.string.lbl_add_from_gallery),
                    getString(R.string.lbl_add_from_camera))
                pictureDialog.setTitle(getString(R.string.title_picture_dialog))
                pictureDialog.setItems(pictureDialogItems){
                    dialog, which ->
                    setPhoto(which)
                    dialog.dismiss()
                }
                pictureDialog.show()
            }
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if(!hasFocus){
            validateInput(v)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        b = null
    }
}