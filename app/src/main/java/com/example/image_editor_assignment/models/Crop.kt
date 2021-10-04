package com.example.image_editor_assignment.models

import android.graphics.Rect

class Crop(private var initialCropRect: Rect) : Operation{

    private var finalCropRect: Rect? = null

    fun setInitialCropRect(cropRect: Rect) {
        initialCropRect = cropRect
    }

    override fun performOperation(operationData: OperationData) {
        finalCropRect = operationData.cropRect
    }

    override fun getOperationType(): OperationType = OperationType.CROP

    override fun getLastOperationInput(): OperationData
        = OperationData(cropRect = initialCropRect, type = OperationType.CROP)

    override fun getLastOperationOutput(): OperationData
        = OperationData(cropRect = finalCropRect, type = OperationType.CROP)

}