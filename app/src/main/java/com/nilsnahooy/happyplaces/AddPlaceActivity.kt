package com.nilsnahooy.happyplaces

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nilsnahooy.happyplaces.databinding.ActivityAddPlaceBinding
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddPlaceActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener {
    companion object {
        const val REQUEST_STORAGE_AND_CAMERA_PERMISSION = 1
    }
    private var b: ActivityAddPlaceBinding? = null
    private var cal = Calendar.getInstance()

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

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

    private var cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val thumbnail: Bitmap = data?.extras?.get("data") as Bitmap
           //todo https://developer.android.com/training/camera2/capture-sessions-requests
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

        b?.tilTitle?.markRequired()
        b?.tilLocation?.markRequired()

        b?.etDate?.setOnClickListener(this)
        b?.tvButtonAddImage?.setOnClickListener(this)
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

    @AfterPermissionGranted(REQUEST_STORAGE_AND_CAMERA_PERMISSION)
    private fun setPhoto(src: Int){
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)){
            val pickFromGallery = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val pickFromCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            when(src){
                0 -> galleryResultLauncher.launch(pickFromGallery)
                1 -> cameraResultLauncher.launch(pickFromCamera)
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.rationale_storage_camera),
                REQUEST_STORAGE_AND_CAMERA_PERMISSION,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
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
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if(!hasFocus){
            validateInput(v)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
            this)
    }

    override fun onDestroy() {
        super.onDestroy()

        b = null
    }
}