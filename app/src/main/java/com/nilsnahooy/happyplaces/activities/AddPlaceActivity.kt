package com.nilsnahooy.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
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
import com.nilsnahooy.happyplaces.HappyPlaceApp
import com.nilsnahooy.happyplaces.R
import com.nilsnahooy.happyplaces.database.HappyPlaceDao
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.nilsnahooy.happyplaces.databinding.ActivityAddPlaceBinding
import com.nilsnahooy.happyplaces.models.HappyPlaceModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddPlaceActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener {
    companion object {
        private const val TAG = "HappyPlacesApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
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
    private var imageUri = ""
    private var mLatitude = 0.0
    private var mLongitude = 0.0
    private var mHappyPlaceDetails: HappyPlaceModel? = null

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var dao: HappyPlaceDao

    private var galleryResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val data: Intent? = result.data
                imageUri = data?.data.toString()
                b?.ivImagePreview?.setImageURI(data?.data)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private var placeResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val place = Autocomplete.getPlaceFromIntent(data!!)
            Toast.makeText(this, place.address, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(b?.root)

        //setup actionbar
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        
        dateSetListener = DatePickerDialog.OnDateSetListener {
                _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            formatDateInField()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    MainActivity.EXTRA_PLACE_DETAILS,
                    HappyPlaceModel::class.java
                )
            }else{
                //ignore deprecation message as we handle newer versions above.
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
            }
        }

        if (mHappyPlaceDetails != null){
            actionBar?.title = "Edit Happy Place"
            b?.etTitle?.setText(mHappyPlaceDetails!!.title)
            b?.etDescription?.setText(mHappyPlaceDetails!!.description)
            b?.etDate?.setText(mHappyPlaceDetails!!.date)
            b?.etLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude
            imageUri = mHappyPlaceDetails!!.imageUri.toString()
            b?.ivImagePreview?.setImageURI(Uri.parse(imageUri))
            b?.btnSave?.text = getString(R.string.btn_update)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        b?.tilTitle?.markRequired()
        b?.tilLocation?.markRequired()

        b?.etDate?.setOnClickListener(this)
        b?.tvButtonAddImage?.setOnClickListener(this)
        b?.btnSave?.setOnClickListener(this)
        b?.btnTakePicture?.setOnClickListener{takePicture()}
        b?.etLocation?.setOnClickListener(this)
        b?.tilTitle?.editText?.onFocusChangeListener = this
        b?.tilLocation?.editText?.onFocusChangeListener = this
        dao = (application as HappyPlaceApp).db.happyPlaceDao()

        //override back navigation as we do not want to have other actions
        // open this Activity unwanted (which they did for some unknown reason...)
        val callback = this.onBackPressedDispatcher.addCallback(this) {
            intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        callback.isEnabled = true

        if(!Places.isInitialized()) {
            Places.initialize(this@AddPlaceActivity,
                getString(R.string.google_places_api_key))
        }
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

    private fun validateInput(v: View?): Boolean{
        val view = v as TextInputEditText
        return if(view.text?.isEmpty() == true || view.text == null){
            view.error = getString(R.string.error_required_field)
            false
        } else {
            view.error = null
            true
        }
    }

    private fun formatDateInField(){
        val myFormat = getString(R.string.fmt_date)
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        b?.etDate?.setText(sdf.format(cal.time).toString())
    }

    //overload of above
    private fun formatDateInField(c:Calendar){
        val myFormat = getString(R.string.fmt_date)
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        b?.etDate?.setText(sdf.format(c.time).toString())
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
                    imageUri = output.savedUri.toString()
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
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
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
            R.id.btn_save -> saveHappyPlace()
            R.id.et_location -> {
                try {
                    val fields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    val intent = Autocomplete
                        .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@AddPlaceActivity)
                    placeResultLauncher.launch(intent)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveHappyPlace(){
        var bitmap: Bitmap? = null
        when {
            !validateInput(b?.etTitle) ->
                Toast.makeText(this, "Place requires a title.", Toast.LENGTH_LONG)
                    .show()
            !validateInput(b?.etLocation) ->
                Toast.makeText(this, "Place needs a location.", Toast.LENGTH_LONG)
                    .show()
            imageUri.isEmpty() ->
                Toast.makeText(this, "Please provide a picture.", Toast.LENGTH_LONG)
                    .show()
            else -> {
                val mainIntent = Intent(this, MainActivity::class.java)
                if (imageUri != mHappyPlaceDetails?.imageUri) {
                    bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                contentResolver,
                                Uri.parse(imageUri)
                            )
                        )
                    } else {
                        //ignore deprecation message as we handle newer versions above.
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(imageUri))
                    }
                }

                //default date if none is chosen
                if (b?.etDate?.text?.isEmpty() == true) {
                    val date = Calendar.getInstance()
                    formatDateInField(date)
                }

                val savableImageUri = if (imageUri == mHappyPlaceDetails?.imageUri) {
                    imageUri
                } else {
                    saveImageToLocalStorage(bitmap!!).toString()
                }

                val placeId = if (mHappyPlaceDetails != null){
                    mHappyPlaceDetails?.id
                } else {
                    0
                }

                val hp = HappyPlaceModel(
                    placeId!!,
                    b?.etTitle?.text.toString(),
                    savableImageUri,
                    b?.etDescription?.text.toString(),
                    b?.etDate?.text.toString(),
                    b?.etLocation?.text.toString(),
                    mLatitude,
                    mLongitude
                )

                lifecycleScope.launch {
                    if (mHappyPlaceDetails == null) {
                        dao.insertHappyPlace(hp)
                    } else {
                        dao.updateHappyPlace(hp)
                    }
                }
                startActivity(mainIntent)
                finish()
            }
        }
    }

    private fun saveImageToLocalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
         try {
             val stream: OutputStream = FileOutputStream(file)
             bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
             stream.flush()
             stream.close()

         }catch (e: IOException){
             e.printStackTrace()
         }
        return Uri.parse(file.absolutePath)
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