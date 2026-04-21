package com.sagar.todoapp.data.repository

import com.sagar.todoapp.data.local.TodoDao
import com.sagar.todoapp.data.local.toEntity
import com.sagar.todoapp.data.remote.TodoApi
import com.sagar.todoapp.data.remote.dto.TodoDto
import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.domain.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Implementation of [TodoRepository] that handles data synchronization between
 * a local Room database and a remote API.
 * This class follows the "offline-first" principle by prioritizing local data
 * and silently handling network failures during synchronization and updates.
 */
class TodoRepositoryImpl(
    private val api: TodoApi,
    private val dao: TodoDao
) : TodoRepository {

    /**
     * Retrieves a stream of todo items from the local database.
     * The data is converted from [TodoEntity] to domain [Todo] objects.
     * Operations are performed on [Dispatchers.Default].
     */
    override fun getTodos(): Flow<List<Todo>> =
        dao.getAllTodos().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.Default)

    /**
     * Synchronizes local data with the remote server.
     * Fetches todos from the API and updates the local database.
     * Fails silently if the network is unavailable.
     */
    override suspend fun syncTodos() {
        try {
            val remote = api.getTodos().map { it.toDomain() }
            dao.upsertAll(remote.map { it.toEntity() })
        } catch (e: Exception) {
            // No internet — Room data is still served, silently ignore
        }
    }

    /**
     * Adds a new todo item.
     * Updates the local database first for immediate UI feedback,
     * then attempts to sync the change with the remote API.
     */
    override suspend fun addTodo(todo: Todo) {
        dao.upsertTodo(todo.toEntity())
        try {
            api.createTodo(TodoDto(todo.id, todo.title, todo.isCompleted))
        } catch (e: Exception) { /* ignore API failure */ }
    }

    /**
     * Updates an existing todo item.
     * Modifies the local database immediately and then synchronizes with the remote server.
     */
    override suspend fun updateTodo(todo: Todo) {
        dao.upsertTodo(todo.toEntity())
        try {
            api.updateTodo(todo.id, TodoDto(todo.id, todo.title, todo.isCompleted))
        } catch (e: Exception) { /* ignore API failure */ }
    }

    /**
     * Deletes a todo item.
     * Removes the item from the local database immediately and then attempts
     * to perform the deletion on the remote server.
     */
    override suspend fun deleteTodo(todo: Todo) {
        dao.deleteTodo(todo.toEntity())
        try {
            api.deleteTodo(todo.id)
        } catch (e: Exception) { /* ignore API failure */ }
    }
}