package com.sagar.todoapp.domain.usecase

import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.domain.repository.TodoRepository

/**
 * Use case to update an existing todo item in the repository.
 */
class UpdateTodoUseCase(private val repository: TodoRepository) {
    /**
     * Executes the update.
     * @param todo The todo item with updated data.
     */
    suspend operator fun invoke(todo: Todo) = repository.updateTodo(todo)
}