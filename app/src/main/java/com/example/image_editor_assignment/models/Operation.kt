package com.example.image_editor_assignment.models

// TODO: Do this operation in ViewModel of ImageEditorFragment
/**
 * Interface defining methods that 'Rotate' and 'Crop' class have to implement
 */
interface Operation {
    fun performOperation(operationData: OperationData)
    fun getOperationType(): OperationType
    fun getLastOperationInput(): OperationData?
    fun getLastOperationOutput(): OperationData?
}