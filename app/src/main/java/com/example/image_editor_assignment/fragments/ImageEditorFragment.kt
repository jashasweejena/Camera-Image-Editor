package com.example.image_editor_assignment.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.image_editor_assignment.databinding.FragmentImageEditorBinding

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
    }

    companion object {
        private const val TAG = "ImageEditorFragment"
    }
}