package com.example.image_editor_assignment.helpers.utils

import android.content.Context
import com.example.image_editor_assignment.R
import com.example.image_editor_assignment.fragments.ImageEditorFragment
import java.io.File

object Util {
    fun createFile(baseFolder: File, format: String, extension: String) =
        File(baseFolder, ImageEditorFragment.fileName + extension)

    /** Use external media if it is available, our app's file directory otherwise */
    fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }
}