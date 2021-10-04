package com.example.image_editor_assignment.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.media.MediaScannerConnection
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.image_editor_assignment.R
import com.example.image_editor_assignment.fragments.ImageEditorFragment
import com.example.image_editor_assignment.helpers.utils.Util
import com.example.image_editor_assignment.models.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageEditorViewModel(application: Application) : AndroidViewModel(application) {

    var operationStack: OperationStack = OperationStack()

    /** Use external media if it is available, our app's file directory otherwise */

    fun performRotation(initialAngle: Float) {
        val rotateOperation: Operation = Rotate(initialAngle = initialAngle)
        rotateOperation.performOperation(OperationData(rotateByAngle = 90f))
        operationStack.addOperation(rotateOperation)
    }

    fun performCrop(crop: Crop, rect: Rect?) {
        crop.performOperation(
            OperationData(
                cropRect = rect,
                type = OperationType.CROP
            )
        )
        operationStack.addOperation(crop)
    }

    fun popStack(): Operation {
        return operationStack.popStack()
    }

    fun isStackEmpty(): Boolean = operationStack.isEmpty()

     fun saveRotatedImage(loadedBitmap: Bitmap): File? {

         if (operationStack.isEmpty()) {
             return null
         }

            val matrix = Matrix()
            matrix.postRotate(0f)
            matrix.postRotate(
                operationStack.peekStack().getLastOperationOutput()?.rotateByAngle?.plus(90f)
                    ?: 0f
            )
        val updatedBitmap = Bitmap.createBitmap(
            loadedBitmap,
            0,
            0,
            loadedBitmap.width,
            loadedBitmap.height,
            matrix,
            true
        )
        val file = Util.createFile(Util.getOutputDirectory(getApplication<Application>().applicationContext), "", ".png")
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension)
        val uri = savebitmap(updatedBitmap, file).absolutePath
        MediaScannerConnection.scanFile(
            getApplication<Application>().applicationContext,
            arrayOf(file.absolutePath),
            arrayOf(mimeType)
        ) { _, uri ->
//                Log.d(ImageEditorFragment.TAG, "Image capture scanned into media store: $uri")
        }
        return file
    }


    private fun savebitmap(bmp: Bitmap, file: File): File {
        val bytes = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes)
        val fo = FileOutputStream(file)
        fo.write(bytes.toByteArray())
        fo.close()
        return file
    }



    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ImageEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ImageEditorViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}



