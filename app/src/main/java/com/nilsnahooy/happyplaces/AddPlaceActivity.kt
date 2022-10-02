package com.nilsnahooy.happyplaces

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nilsnahooy.happyplaces.databinding.ActivityAddPlaceBinding
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener {
    private var b: ActivityAddPlaceBinding? = null
    private var cal = Calendar.getInstance()

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

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

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.et_date -> {
                DatePickerDialog(this@AddPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if(!hasFocus){
            validateInput(v)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        b = null
    }
}