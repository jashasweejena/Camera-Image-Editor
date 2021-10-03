package com.example.image_editor_assignment.helpers.extensions

import android.animation.Animator
import android.widget.ImageView

     fun ImageView.rotateImage(angle: Float, animationCallback: Animator.AnimatorListener? = null) {
            val h = this.height
            val w = this.width

            val scaleFactor: Float =
                if (this.rotation / angle % 2 == 0f) (w * 1.0f / h) else 1f

            this.animate()?.rotationBy(angle)?.scaleX(scaleFactor)?.scaleY(scaleFactor)
                ?.setListener(animationCallback)



    }
