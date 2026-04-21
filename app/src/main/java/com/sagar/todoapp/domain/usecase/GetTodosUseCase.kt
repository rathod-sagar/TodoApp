package com.sagar.todoapp.domain.usecase

import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve all todo items from the repository as a stream.
 */
class GetTodosUseCase(private val repository: TodoRepository) {
    /**
     * Executes the use case.
     * @return A [Flow] emitting the list of todos.
     */
    operator fun invoke(): Flow<List<Todo>> = repository.getTodos()
}