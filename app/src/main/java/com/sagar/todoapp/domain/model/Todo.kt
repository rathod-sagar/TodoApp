package com.sagar.todoapp.domain.model

import androidx.compose.runtime.Immutable

/**
 * Domain model representing a Todo item.
 * This class is used across the domain and UI layers.
 * It is marked as [Immutable] to optimize Jetpack Compose recompositions.
 *
 * @property id Unique identifier for the todo.
 * @property title The description or title of the todo.
 * @property isCompleted Whether the todo task has been finished.
 */
@Immutable
data class Todo(
    val id: Int,
    val title: String,
    val isCompleted: Boolean
)
