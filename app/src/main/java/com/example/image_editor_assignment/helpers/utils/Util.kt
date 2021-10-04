package com.example.image_editor_assignment.helpers.utils

import com.example.image_editor_assignment.fragments.ImageEditorFragment
import java.io.File

object Util {
    fun createFile(baseFolder: File, format: String, extension: String) =
        File(baseFolder, ImageEditorFragment.fileName + extension)
}