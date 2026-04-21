package com.sagar.todoapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sagar.todoapp.domain.model.Todo

/**
 * Room entity representing a todo item in the local database.
 * This class defines the schema for the "todos" table.
 *
 * @property id Unique identifier for the todo item.
 * @property title The content or title of the todo.
 * @property isCompleted Indicates whether the todo has been finished.
 */
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val isCompleted: Boolean
) {
    /**
     * Converts the database entity to a domain-level [Todo] object.
     */
    fun toDomain() = Todo(id = id, title = title, isCompleted = isCompleted)
}

/**
 * Extension function to convert a domain-level [Todo] object to its database entity representation.
 */
fun Todo.toEntity() = TodoEntity(id = id, title = title, isCompleted = isCompleted)