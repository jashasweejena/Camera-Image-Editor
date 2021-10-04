package com.example.image_editor_assignment.viewmodels

import android.app.Application
import android.media.MediaScannerConnection
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.net.toFile
import androidx.lifecycle.*

class CameraPreviewViewModel(application: Application) : AndroidViewModel(application) {

    fun saveImage(savedUri: Uri) {
//        val savedUri = output.savedUri ?: Uri.fromFile(outputImageFile)

        // If the folder selected is an external media directory, this is
        // unnecessary but otherwise other apps will not be able to access our
        // images unless we scan them using [MediaScannerConnection]

        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(savedUri.toFile().extension)
        MediaScannerConnection.scanFile(
            getApplication<Application>().applicationContext,
            arrayOf(savedUri.toFile().absolutePath),
            arrayOf(mimeType)
        ) { _, uri ->
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraPreviewViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CameraPreviewViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}