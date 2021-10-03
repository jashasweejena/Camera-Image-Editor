package com.example.image_editor_assignment.models

/**
 * Helps keep track of Rotate operations
 */
class Rotate(initialAngle: Float) : Operation {
    private var currentAngle = initialAngle
        get() = field

    private var rotateByAngle = -1f

    private var operationData: OperationData? = null

    private var lastOperationResult: OperationData? = null

    private fun rotateByAngle(angle: Float) {
        rotateByAngle = angle
        currentAngle +=  rotateByAngle
    }

    // Can be called from outside
    override fun performOperation(operationData: OperationData) {
        operationData.rotateByAngle?.let {
            rotateByAngle(it)
        }
        this.operationData = operationData
        this.lastOperationResult = OperationData(rotateByAngle = this.currentAngle)
    }

    override fun getOperationType() = OperationType.ROTATE
    override fun getLastOperationInput(): OperationData? = operationData
    override fun getLastOperationOutput(): OperationData? = lastOperationResult
}