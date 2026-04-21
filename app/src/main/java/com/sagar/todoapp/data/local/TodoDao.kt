package com.sagar.todoapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the "todos" table.
 * Defines the database operations for managing [TodoEntity] objects.
 */
@Dao
interface TodoDao {

    /**
     * Retrieves all todo items from the database.
     * Returns a [Flow] that emits a new list whenever the data in the "todos" table changes.
     */
    @Query("SELECT * FROM todos")
    fun getAllTodos(): Flow<List<TodoEntity>>

    /**
     * Inserts a [TodoEntity] into the database or updates it if it already exists (Upsert).
     * @param todo The todo item to be saved.
     */
    @Upsert
    suspend fun upsertTodo(todo: TodoEntity)

    /**
     * Inserts or updates a list of [TodoEntity] objects.
     * This is commonly used for syncing local data with remote sources.
     * @param todos The list of todo items to be saved.
     */
    @Upsert
    suspend fun upsertAll(todos: List<TodoEntity>)

    /**
     * Deletes a specific [TodoEntity] from the database.
     * @param todo The todo item to be removed.
     */
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
}