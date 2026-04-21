package com.sagar.todoapp.domain.usecase

import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.domain.repository.TodoRepository

/**
 * Use case to delete a specific todo item from the repository.
 */
class DeleteTodoUseCase(private val repository: TodoRepository) {
    /**
     * Executes the deletion.
     * @param todo The todo item to be removed.
     */
    suspend operator fun invoke(todo: Todo) = repository.deleteTodo(todo)
}