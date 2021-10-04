package com.example.image_editor_assignment.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.image_editor_assignment.databinding.FragmentImageEditorBinding

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Rect
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toFile
import com.example.image_editor_assignment.R
import com.example.image_editor_assignment.helpers.extensions.*
import com.example.image_editor_assignment.models.*
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream


class ImageEditorFragment : Fragment() {
    val args: ImageEditorFragmentArgs by navArgs()
    var imageEditorBinding: FragmentImageEditorBinding? = null

    var lastCropRectData: Rect? = null
    var lastCropPoints: FloatArray? = null

    //TODO: Put the data handling part into ViewModel
    var operationStack: OperationStack = OperationStack()

    var latestRotationOperation: Rotate? = null

    lateinit var loadedBitmap: Bitmap
    lateinit var loadedImageUri: String
    lateinit var cropActivityResultLauncher: ActivityResultLauncher<Uri?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadedImageUri = args.imageUri
        val cropActivityResultContract = object : ActivityResultContract<Uri?, Uri?>() {
            var crop: Crop = Crop(Rect())
            override fun createIntent(context: Context, input: Uri?): Intent {
                return CropImage.activity(input)
                    .getIntent(requireContext())
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                val result = CropImage.getActivityResult(intent)
                if (lastCropRectData != result?.cropRect) {
                    crop.performOperation(
                        OperationData(
                            cropRect = result?.cropRect,
                            type = OperationType.CROP
                        )
                    )
                    operationStack.addOperation(crop)
                    imageEditorBinding?.undoBtn?.isEnabled = true
                    lastCropRectData = result?.cropRect
                    lastCropPoints = result?.cropPoints
                }
                return result?.uri
            }

        }

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContract) {
            Log.d(TAG, "onCreate: " + it.toString())
            it?.let { uri ->
                imageEditorBinding?.imageView?.let { imageView ->
                    Glide.with(imageView.context)
                        .load(uri)
                        .into(imageView)
                }
            }
        }

            loadedBitmap = MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                Uri.parse(args.imageUri)
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        imageEditorBinding = FragmentImageEditorBinding.inflate(inflater, container, false)
        return imageEditorBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageEditorBinding?.imageView?.let { Glide.with(it.context).load(args.imageUri).into(it) }



        imageEditorBinding?.rotateBtn?.setOnClickListener {
            imageEditorBinding?.imageView?.rotation?.let {
                //TODO: Put the rotate Operation into the extension function somehow to make it seamless
                val rotateOperation: Operation = Rotate(initialAngle = it)
                rotateOperation.performOperation(OperationData(rotateByAngle = 90f))
                operationStack.addOperation(rotateOperation)
                latestRotationOperation = rotateOperation as Rotate

//                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, )
                imageEditorBinding?.imageView?.rotateImage(90f, object : Animator.AnimatorListener {

                    override fun onAnimationStart(p0: Animator?) {
                        imageEditorBinding?.rotateBtn?.isEnabled = false
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        imageEditorBinding?.rotateBtn?.isEnabled = true
                    }

                    override fun onAnimationCancel(p0: Animator?) = Unit

                    override fun onAnimationRepeat(p0: Animator?) = Unit

                })

                imageEditorBinding?.undoBtn?.isEnabled = true
            }
        }
        imageEditorBinding?.undoBtn?.setOnClickListener {
            if (!operationStack.isEmpty()) {
                val lastOperation = operationStack.popStack()
                when (lastOperation.getOperationType()) {
                    OperationType.ROTATE -> {
                        lastOperation.getLastOperationInput()?.rotateByAngle?.let { angle ->
                            imageEditorBinding?.imageView?.rotateImage(-1f * angle)
                        }
                    }
                    OperationType.CROP -> {
                        // Get crop rect data
                        imageEditorBinding?.imageView?.let { imageView ->
                            Glide.with(imageView.context)
                                .load(args.imageUri)
                                .into(imageView)
                        }
                    }
                }

            }

            if (operationStack.isEmpty()) {
                it.isEnabled = false
            }

        }

        imageEditorBinding?.cropBtn?.setOnClickListener {
            val rotatedFile = saveRotatedImage()
            var uriString = args.imageUri
            rotatedFile?.absolutePath?.let {
                uriString = "file://$it"
            }
            cropActivityResultLauncher.launch(Uri.parse(uriString))
        }

        imageEditorBinding?.saveBtn?.setOnClickListener {
            saveRotatedImage()
        }
    }

    private fun saveRotatedImage(): File? {
        if (!operationStack.isEmpty()) {
            val matrix = Matrix()
            matrix.postRotate(0f)
            imageEditorBinding?.imageView?.rotation?.let { it1 ->
                matrix.postRotate(
                    latestRotationOperation?.getLastOperationOutput()?.rotateByAngle?.plus(90f)
                        ?: 0f
                )
            }
            val updatedBitmap = Bitmap.createBitmap(
                loadedBitmap,
                0,
                0,
                loadedBitmap.width,
                loadedBitmap.height,
                matrix,
                true
            )
            val file = createFile(getOutputDirectory(requireContext()), "", ".png")
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(file.extension)
            val uri = savebitmap(updatedBitmap, file).absolutePath
            loadedImageUri = uri
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf(mimeType)
            ) { _, uri ->
                Log.d(TAG, "Image capture scanned into media store: $uri")
            }
            Toast.makeText(context, "onViewCreated: File Saved: $uri", Toast.LENGTH_SHORT)
            Log.d(TAG, "onViewCreated: File Saved: $uri")
            return file
        }
        return null
    }
    private fun savebitmap(bmp: Bitmap, file: File): File {
        val bytes = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes)
        val fo = FileOutputStream(file)
        fo.write(bytes.toByteArray())
        fo.close()
        return file
    }


    companion object {
        private const val TAG = "ImageEditorFragment"
        var fileName = "${System.currentTimeMillis()}"
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, fileName + extension)

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }



    }
}