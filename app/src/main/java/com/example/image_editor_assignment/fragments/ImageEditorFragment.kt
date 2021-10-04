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
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.example.image_editor_assignment.helpers.extensions.*
import com.example.image_editor_assignment.models.*
import com.theartofdev.edmodo.cropper.CropImage

class ImageEditorFragment : Fragment() {
    val args: ImageEditorFragmentArgs by navArgs()
    var imageEditorBinding: FragmentImageEditorBinding? = null

    var lastCropRectData: Rect? = null
    var lastCropPoints: FloatArray? = null

    //TODO: Put the data handling part into ViewModel
    var operationStack: OperationStack = OperationStack()

    lateinit var cropActivityResultLauncher: ActivityResultLauncher<Uri?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            cropActivityResultLauncher.launch(Uri.parse(args.imageUri))
        }
    }

    companion object {
        private const val TAG = "ImageEditorFragment"
    }
}