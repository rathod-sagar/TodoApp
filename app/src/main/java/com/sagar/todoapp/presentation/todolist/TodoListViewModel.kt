package com.sagar.todoapp.presentation.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.domain.usecase.AddTodoUseCase
import com.sagar.todoapp.domain.usecase.DeleteTodoUseCase
import com.sagar.todoapp.domain.usecase.GetTodosUseCase
import com.sagar.todoapp.domain.usecase.SyncTodosUseCase
import com.sagar.todoapp.domain.usecase.UpdateTodoUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Todo List screen.
 * Manages the UI state and handles user events by interacting with the domain-layer use cases.
 * It follows the MVI-like pattern, exposing a [uiState] for the view to observe and
 * an [onEvent] method to receive user actions.
 */
class TodoListViewModel(
    private val getTodosUseCase: GetTodosUseCase,
    private val syncTodosUseCase: SyncTodosUseCase,
    private val addTodoUseCase: AddTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase
) : ViewModel() {

    private var hasLoadedInitialData = false

    // Internal state flow to manage TodoUiState.
    private val _uiState = MutableStateFlow<TodoUiState>(TodoUiState.Loading)

    /**
     * Public UI state observed by the Compose UI.
     * Automatically triggers initial data observation and sync when first subscribed to.
     */
    val uiState = _uiState.onStart {
        if (!hasLoadedInitialData) {
            observeTodos()
            syncTodos()
            hasLoadedInitialData = true
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = TodoUiState.Loading
    )

    // Channel for one-time UI events like showing Snakbars.
    private val _uiEvent = Channel<TodoUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    /**
     * Entry point for all user interactions from the UI.
     */
    fun onEvent(event: TodoEvent) {
        when (event) {
            is TodoEvent.AddTodo -> addTodo(event.title)
            is TodoEvent.DeleteTodo -> deleteTodo(event.todo)
            is TodoEvent.ToggleTodo -> toggleTodo(event.todo)
            is TodoEvent.EditTodo -> editTodo(event.todo, event.newTitle)
        }
    }

    /**
     * Starts observing the stream of todos from the local database.
     */
    private fun observeTodos() {
        viewModelScope.launch {
            try {
                getTodosUseCase().collect { todos ->
                    _uiState.value = TodoUiState.Success(todos)
                }
            } catch (e: Exception) {
                _uiState.value = TodoUiState.Error("Failed to load todos.")
            }
        }
    }

    /**
     * Triggers a remote sync to fetch latest todos from the API.
     */
    private fun syncTodos() {
        viewModelScope.launch {
            try {
                syncTodosUseCase()
            } catch (e: Exception) { /* handled in repository */ }
        }
    }

    /**
     * Handles adding a new todo item.
     */
    private fun addTodo(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val localId = System.currentTimeMillis().toInt()
                addTodoUseCase(Todo(id = localId, title = title.trim(), isCompleted = false))
            } catch (e: Exception) {
                sendError("Failed to add todo.")
            }
        }
    }

    /**
     * Handles toggling the completion status of a todo.
     */
    private fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                updateTodoUseCase(todo.copy(isCompleted = !todo.isCompleted))
            } catch (e: Exception) {
                sendError("Failed to update todo.")
            }
        }
    }

    /**
     * Handles updating the title of an existing todo.
     */
    private fun editTodo(todo: Todo, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            try {
                updateTodoUseCase(todo.copy(title = newTitle.trim()))
            } catch (e: Exception) {
                sendError("Failed to edit todo.")
            }
        }
    }

    /**
     * Handles deleting a todo item.
     */
    private fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                deleteTodoUseCase(todo)
            } catch (e: Exception) {
                sendError("Failed to delete todo.")
            }
        }
    }

    /**
     * Sends a one-time event to the UI (e.g., error message).
     */
    private fun sendError(message: String) {
        viewModelScope.launch {
            _uiEvent.send(TodoUiEvent.ShowSnackbar(message))
        }
    }
}

/**
 * Sealed class for one-time UI events that shouldn't be part of the persistent state.
 */
sealed class TodoUiEvent {
    data class ShowSnackbar(val message: String) : TodoUiEvent()
}
