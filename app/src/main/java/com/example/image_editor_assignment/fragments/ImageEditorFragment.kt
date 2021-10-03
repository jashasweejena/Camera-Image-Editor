package com.example.image_editor_assignment.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.image_editor_assignment.databinding.FragmentImageEditorBinding

import android.animation.Animator
import com.example.image_editor_assignment.models.Operation
import com.example.image_editor_assignment.models.OperationData
import com.example.image_editor_assignment.models.OperationStack
import com.example.image_editor_assignment.models.Rotate
import com.example.image_editor_assignment.helpers.extensions.*


class ImageEditorFragment : Fragment() {
    val args: ImageEditorFragmentArgs by navArgs()
    var imageEditorBinding: FragmentImageEditorBinding? = null

    var operationStack: OperationStack = OperationStack()

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
                lastOperation.getLastOperationInput()?.rotateByAngle?.let { angle ->
                    imageEditorBinding?.imageView?.rotateImage(-1f * angle)
                }
            }

            if (operationStack.isEmpty()) {
                it.isEnabled = false
            }

        }
    }

    companion object {
        private const val TAG = "ImageEditorFragment"
    }
}