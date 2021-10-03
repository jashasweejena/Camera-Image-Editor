package com.example.image_editor_assignment.models

/**
 * Data Type to store data of Crop and Rotate operation
 */
data class OperationData(
    var rotateByAngle: Float? = null,
    var crop: Int? = null,
    var type: OperationType? = null
)

enum class OperationType {
    CROP,
    ROTATE
}
