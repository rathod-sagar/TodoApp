package com.sagar.todoapp.domain.repository

import com.sagar.todoapp.domain.model.Todo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing todo items.
 * This defines the contract for data operations, abstracted from the underlying data sources.
 */
interface TodoRepository {
    /**
     * Observes the list of todos.
     * @return A [Flow] emitting the current list of todos.
     */
    fun getTodos(): Flow<List<Todo>>

    /**
     * Synchronizes local data with the remote source.
     */
    suspend fun syncTodos()

    /**
     * Adds a new todo item.
     * @param todo The todo to add.
     */
    suspend fun addTodo(todo: Todo)

    /**
     * Updates an existing todo item.
     * @param todo The todo with updated information.
     */
    suspend fun updateTodo(todo: Todo)

    /**
     * Deletes a todo item.
     * @param todo The todo to delete.
     */
    suspend fun deleteTodo(todo: Todo)
}