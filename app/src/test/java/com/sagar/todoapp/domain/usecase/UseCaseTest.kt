package com.sagar.todoapp.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.domain.repository.TodoRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.assertFailsWith

import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UseCaseTest {

    private val repository: TodoRepository = mockk()

    private val fakeTodos = listOf(
        Todo(id = 1, title = "Buy groceries", isCompleted = false),
        Todo(id = 2, title = "Walk the dog", isCompleted = true)
    )

    // GetTodosUseCase
    @Test
    fun `GetTodosUseCase returns flow from repository`() = runTest {
        every { repository.getTodos() } returns flowOf(fakeTodos)

        val result = GetTodosUseCase(repository)().first()

        assertThat(result).isEqualTo(fakeTodos)
    }

    // SyncTodosUseCase
    @Test
    fun `SyncTodosUseCase calls repository syncTodos`() = runTest {
        coEvery { repository.syncTodos() } just Runs

        SyncTodosUseCase(repository)()

        coVerify(exactly = 1) { repository.syncTodos() }
    }

    // AddTodoUseCase
    @Test
    fun `AddTodoUseCase calls repository addTodo with correct todo`() = runTest {
        coEvery { repository.addTodo(any()) } just Runs
        val todo = fakeTodos[0]

        AddTodoUseCase(repository)(todo)

        coVerify(exactly = 1) { repository.addTodo(todo) }
    }

    // UpdateTodoUseCase
    @Test
    fun `UpdateTodoUseCase calls repository updateTodo with correct todo`() = runTest {
        coEvery { repository.updateTodo(any()) } just Runs
        val todo = fakeTodos[0]

        UpdateTodoUseCase(repository)(todo)

        coVerify(exactly = 1) { repository.updateTodo(todo) }
    }

    // DeleteTodoUseCase
    @Test
    fun `DeleteTodoUseCase calls repository deleteTodo with correct todo`() = runTest {
        coEvery { repository.deleteTodo(any()) } just Runs
        val todo = fakeTodos[0]

        DeleteTodoUseCase(repository)(todo)

        coVerify(exactly = 1) { repository.deleteTodo(todo) }
    }

    // GetTodosUseCase edge cases
    @Test
    fun `GetTodosUseCase returns empty list when repository is empty`() = runTest {
        every { repository.getTodos() } returns flowOf(emptyList())

        val result = GetTodosUseCase(repository)().first()

        assertThat(result).isEmpty()
    }

    @Test
    fun `GetTodosUseCase propagates exception from repository`() = runTest {
        every { repository.getTodos() } throws RuntimeException("DB error")

        assertFailsWith<RuntimeException> {
            GetTodosUseCase(repository)().first()
        }
    }

    // SyncTodosUseCase failure
    @Test
    fun `SyncTodosUseCase propagates exception when repository throws`() = runTest {
        coEvery { repository.syncTodos() } throws RuntimeException("Network error")

        assertFailsWith<RuntimeException> {
            SyncTodosUseCase(repository)()
        }
    }

    // AddTodoUseCase edge cases
    @Test
    fun `AddTodoUseCase propagates exception when repository throws`() = runTest {
        coEvery { repository.addTodo(any()) } throws RuntimeException("DB error")

        assertFailsWith<RuntimeException> {
            AddTodoUseCase(repository)(fakeTodos[0])
        }
    }

    @Test
    fun `AddTodoUseCase handles todo with empty title`() = runTest {
        val emptyTitleTodo = Todo(id = 3, title = "", isCompleted = false)
        coEvery { repository.addTodo(any()) } just Runs

        AddTodoUseCase(repository)(emptyTitleTodo)

        // Use case itself doesn't validate — that's the ViewModel's job
        coVerify(exactly = 1) { repository.addTodo(emptyTitleTodo) }
    }

    // UpdateTodoUseCase edge cases
    @Test
    fun `UpdateTodoUseCase propagates exception when repository throws`() = runTest {
        coEvery { repository.updateTodo(any()) } throws RuntimeException("DB error")

        assertFailsWith<RuntimeException> {
            UpdateTodoUseCase(repository)(fakeTodos[0])
        }
    }

    @Test
    fun `UpdateTodoUseCase can mark todo as completed`() = runTest {
        coEvery { repository.updateTodo(any()) } just Runs
        val completedTodo = fakeTodos[0].copy(isCompleted = true)

        UpdateTodoUseCase(repository)(completedTodo)

        coVerify { repository.updateTodo(match { it.isCompleted }) }
    }

    @Test
    fun `UpdateTodoUseCase can mark todo as incomplete`() = runTest {
        coEvery { repository.updateTodo(any()) } just Runs
        val incompleteTodo = fakeTodos[1].copy(isCompleted = false)

        UpdateTodoUseCase(repository)(incompleteTodo)

        coVerify { repository.updateTodo(match { !it.isCompleted }) }
    }

    // DeleteTodoUseCase edge cases
    @Test
    fun `DeleteTodoUseCase propagates exception when repository throws`() = runTest {
        coEvery { repository.deleteTodo(any()) } throws RuntimeException("DB error")

        assertFailsWith<RuntimeException> {
            DeleteTodoUseCase(repository)(fakeTodos[0])
        }
    }
}