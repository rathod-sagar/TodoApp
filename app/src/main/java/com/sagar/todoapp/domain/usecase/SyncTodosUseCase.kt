package com.sagar.todoapp.domain.usecase

import com.sagar.todoapp.domain.repository.TodoRepository

/**
 * Use case to synchronize local todo data with the remote source.
 */
class SyncTodosUseCase(private val repository: TodoRepository) {
    /**
     * Executes the synchronization process.
     */
    suspend operator fun invoke() = repository.syncTodos()
}