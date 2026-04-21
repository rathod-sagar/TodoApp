package com.sagar.todoapp.domain.usecase

import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.domain.repository.TodoRepository

/**
 * Use case to add a new todo item to the repository.
 */
class AddTodoUseCase(private val repository: TodoRepository) {
    /**
     * Executes the use case.
     * @param todo The todo item to be added.
     */
    suspend operator fun invoke(todo: Todo) = repository.addTodo(todo)
}