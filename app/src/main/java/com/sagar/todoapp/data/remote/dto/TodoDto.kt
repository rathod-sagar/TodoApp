package com.sagar.todoapp.data.remote.dto

import com.sagar.todoapp.domain.model.Todo

/**
 * Data Transfer Object (DTO) representing a todo item received from the remote API.
 * This class is used for parsing JSON responses from network calls.
 *
 * @property id The unique identifier of the todo from the server.
 * @property title The title or description of the todo.
 * @property completed The completion status of the todo as returned by the API.
 */
data class TodoDto(
    val id: Int,
    val title: String,
    val completed: Boolean
) {
    /**
     * Converts this remote DTO into a domain-level [Todo] model.
     */
    fun toDomain() = Todo(id = id, title = title, isCompleted = completed)
}