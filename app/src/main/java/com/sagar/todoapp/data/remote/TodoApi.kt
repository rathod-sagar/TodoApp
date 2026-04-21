package com.sagar.todoapp.data.remote

import com.sagar.todoapp.data.remote.dto.TodoDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit interface defining the remote API endpoints for managing todo items.
 * Interacts with the backend service to perform CRUD operations.
 */
interface TodoApi {
    /**
     * Fetches the full list of todo items from the server.
     * @return A list of [TodoDto] objects.
     */
    @GET("todos")
    suspend fun getTodos(): List<TodoDto>

    /**
     * Creates a new todo item on the server.
     * @param todo The [TodoDto] containing the new todo data.
     * @return The created [TodoDto] returned by the server.
     */
    @POST("todos")
    suspend fun createTodo(@Body todo: TodoDto): TodoDto

    /**
     * Updates an existing todo item on the server.
     * @param id The unique identifier of the todo to update.
     * @param todo The updated [TodoDto] data.
     * @return The updated [TodoDto] returned by the server.
     */
    @PUT("todos/{id}")
    suspend fun updateTodo(@Path("id") id: Int, @Body todo: TodoDto): TodoDto

    /**
     * Deletes a specific todo item from the server.
     * @param id The unique identifier of the todo to be removed.
     */
    @DELETE("todos/{id}")
    suspend fun deleteTodo(@Path("id") id: Int)
}