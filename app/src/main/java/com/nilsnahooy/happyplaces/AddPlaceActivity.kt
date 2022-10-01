package com.nilsnahooy.happyplaces

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nilsnahooy.happyplaces.databinding.ActivityAddPlaceBinding

class AddPlaceActivity : AppCompatActivity() {
    private var b: ActivityAddPlaceBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(b?.root)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        b?.tilTitle?.markRequired()
        b?.tilLocation?.markRequired()

        b?.tilTitle?.editText?.setOnFocusChangeListener {
                v, hasFocus ->
            if (!hasFocus){
                validateInput(v)
            }
        }

        b?.tilLocation?.editText?.setOnFocusChangeListener {
                v, hasFocus ->
            if(!hasFocus){
                validateInput(v)
            }
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

    private fun validateInput(v: View){
        val view = v as TextInputEditText
        if(view.text?.isEmpty() == true || view.text == null){
            view.error = "required field!"
        } else {
            view.error = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        b = null
    }
}