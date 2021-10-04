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
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.ViewModelProvider
import com.example.image_editor_assignment.helpers.extensions.*
import com.example.image_editor_assignment.models.*
import com.theartofdev.edmodo.cropper.CropImage
import com.example.image_editor_assignment.viewmodels.ImageEditorViewModel


class ImageEditorFragment : Fragment() {
    val args: ImageEditorFragmentArgs by navArgs()
    var imageEditorBinding: FragmentImageEditorBinding? = null

    var lastCropRectData: Rect? = null
    var lastCropPoints: FloatArray? = null

    lateinit var viewModel: ImageEditorViewModel

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
                    viewModel.performCrop(crop, result?.cropRect)
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
        viewModel = ViewModelProvider(this, ImageEditorViewModel.Factory(requireActivity().application))
            .get(ImageEditorViewModel::class.java)

        imageEditorBinding?.imageView?.let { Glide.with(it.context).load(args.imageUri).into(it) }



        imageEditorBinding?.rotateBtn?.setOnClickListener {
            imageEditorBinding?.imageView?.rotation?.let {
                viewModel.performRotation(initialAngle = it)
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
            if (!viewModel.isStackEmpty()) {
                val lastOperation = viewModel.popStack()
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

            if (viewModel.isStackEmpty()) {
                it.isEnabled = false
            }

        }

        imageEditorBinding?.cropBtn?.setOnClickListener {
            val rotatedFile = viewModel.saveRotatedImage(loadedBitmap)
            var uriString = args.imageUri
            rotatedFile?.absolutePath?.let {
                uriString = "file://$it"
            }
            cropActivityResultLauncher.launch(Uri.parse(uriString))
        }

        imageEditorBinding?.saveBtn?.setOnClickListener {
            viewModel.saveRotatedImage(loadedBitmap)
        }
    }

    companion object {
        private const val TAG = "ImageEditorFragment"
        var fileName = "${System.currentTimeMillis()}"

    }
}