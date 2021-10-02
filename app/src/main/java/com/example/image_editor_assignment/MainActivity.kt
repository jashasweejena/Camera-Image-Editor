package com.example.image_editor_assignment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.*

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        var galleryPickerImage = this.registerForActivityResult(GetContent()) { uri: Uri? ->
//            Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
//        }
//
//        val openGallery = findViewById<Button>(R.id.openGalleryBtn)
//        openGallery.setOnClickListener {
//            galleryPickerImage.launch("image/*")
//        }
    }
}