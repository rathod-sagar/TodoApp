package com.sagar.todoapp.presentation.todolist

import com.sagar.todoapp.domain.model.Todo

/**
 * Sealed class representing user interactions or events in the Todo List screen.
 * This follows the MVI (Model-View-Intent) pattern where UI events are dispatched to the ViewModel.
 */
sealed class TodoEvent {
    /**
     * Event to add a new todo with the given title.
     */
    data class AddTodo(val title: String) : TodoEvent()

    /**
     * Event to delete an existing todo.
     */
    data class DeleteTodo(val todo: Todo) : TodoEvent()

    /**
     * Event to toggle the completion status of a todo.
     */
    data class ToggleTodo(val todo: Todo) : TodoEvent()

    /**
     * Event to update the title of an existing todo.
     */
    data class EditTodo(val todo: Todo, val newTitle: String) : TodoEvent()
}