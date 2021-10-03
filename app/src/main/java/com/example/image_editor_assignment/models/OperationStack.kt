package com.example.image_editor_assignment.models

import java.util.*

/**
 * Custom Class to keep track of [Rotate] and Crop operation
 * Helps undo changes
 */
class OperationStack {
    private var stack: Stack<Operation> = Stack()

    /**
     * Pushes operation to [OperationStack]
     * @param operation Item to be pushed
     */
    fun addOperation(operation: Operation): Operation = stack.push(operation)

    /**
     * Pops (removes and return) last item from [OperationStack]
     */
    fun popStack(): Operation = stack.pop()

    /**
     * Peeks (return) last item from [OperationStack]
     * @return item Last item if [OperationStac] is not empty, null otherwise
     */
    fun peekStack(): Operation = stack.peek()

    fun isEmpty(): Boolean = stack.isEmpty()
}
