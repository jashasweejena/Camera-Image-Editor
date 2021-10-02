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


class ImageEditorFragment : Fragment() {
    val args: ImageEditorFragmentArgs by navArgs()
    var imageEditorBinding: FragmentImageEditorBinding? = null

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
            rotateImage(-90f, imageEditorBinding?.imageView)
        }
    }

    private fun rotateImage(angle: Float, imageView: ImageView?) {

        imageView?.let { imageView ->
            val h = imageView.height
            val w = imageView.width

            val scaleFactor: Float =
                if (imageView.rotation / angle % 2 == 0f) (w * 1.0f / h) else 1f

            imageView.animate()?.rotationBy(angle)?.scaleX(scaleFactor)?.scaleY(scaleFactor)
                ?.setListener(
                    object : Animator.AnimatorListener {
                        override fun onAnimationStart(p0: Animator?) {
                            imageEditorBinding?.rotateBtn?.isEnabled = false
                        }

                        override fun onAnimationEnd(p0: Animator?) {
                            imageEditorBinding?.rotateBtn?.isEnabled = true
                        }

                        override fun onAnimationCancel(p0: Animator?) = Unit

                        override fun onAnimationRepeat(p0: Animator?) = Unit

                    })
        }


    }

    companion object {
        private const val TAG = "ImageEditorFragment"
    }
}