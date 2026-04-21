package com.sagar.todoapp.di

import androidx.room.Room
import com.sagar.todoapp.data.local.TodoDatabase
import com.sagar.todoapp.data.remote.TodoApi
import com.sagar.todoapp.data.repository.TodoRepositoryImpl
import com.sagar.todoapp.domain.repository.TodoRepository
import com.sagar.todoapp.domain.usecase.AddTodoUseCase
import com.sagar.todoapp.domain.usecase.DeleteTodoUseCase
import com.sagar.todoapp.domain.usecase.GetTodosUseCase
import com.sagar.todoapp.domain.usecase.SyncTodosUseCase
import com.sagar.todoapp.domain.usecase.UpdateTodoUseCase
import com.sagar.todoapp.presentation.todolist.TodoListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Koin module for dependency injection.
 * This module defines how to provide various dependencies throughout the application,
 * including local database, network services, repositories, use cases, and view models.
 */
val appModule = module {

    // --- Database Dependencies ---
    // Provides a singleton instance of the Room database.
    single {
        Room.databaseBuilder(androidContext(), TodoDatabase::class.java, "todo_db")
            .build()
    }
    // Provides the Data Access Object (DAO) for Todo operations.
    single { get<TodoDatabase>().todoDao() }

    // --- Network Dependencies ---
    // Provides a singleton instance of TodoApi configured with Retrofit.
    single {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TodoApi::class.java)
    }

    // --- Repository Dependencies ---
    // Maps the TodoRepository interface to its implementation, injecting the DAO and API.
    single<TodoRepository> { TodoRepositoryImpl(get(), get()) }

    // --- Use Case Dependencies ---
    // Use cases are provided as factory instances, as they are often short-lived and stateless.
    factory { GetTodosUseCase(get()) }
    factory { AddTodoUseCase(get()) }
    factory { UpdateTodoUseCase(get()) }
    factory { DeleteTodoUseCase(get()) }
    factory { SyncTodosUseCase(get()) }

    // --- View Model Dependencies ---
    // Defines how to create the TodoListViewModel with all its necessary use cases.
    viewModel {
        TodoListViewModel(
            getTodosUseCase = get(),
            syncTodosUseCase = get(),
            addTodoUseCase = get(),
            updateTodoUseCase = get(),
            deleteTodoUseCase = get(),
        )
    }
}