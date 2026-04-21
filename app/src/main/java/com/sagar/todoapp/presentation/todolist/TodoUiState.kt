package com.sagar.todoapp.presentation.todolist

import androidx.compose.runtime.Immutable
import com.sagar.todoapp.domain.model.Todo

/**
 * Sealed class representing the different states of the Todo List UI.
 */
sealed class TodoUiState {
    /**
     * Represents the initial or loading state while data is being fetched.
     */
    object Loading : TodoUiState()

    /**
     * Represents an error state when data fetching or an operation fails.
     * @property message A descriptive error message to be displayed.
     */
    data class Error(val message: String) : TodoUiState()

    /**
     * Represents a successful state where the todo items are ready to be displayed.
     * Marked as [Immutable] for Compose performance.
     *
     * @property todos The list of todo items.
     * @property completedCount Calculated number of completed todos.
     * @property progress Calculated completion progress as a float between 0.0 and 1.0.
     */
    @Immutable
    data class Success(
        val todos: List<Todo>,
        val completedCount: Int = todos.count { it.isCompleted },
        val progress: Float = if (todos.isEmpty()) 0f else completedCount.toFloat() / todos.size
    ) : TodoUiState()
}