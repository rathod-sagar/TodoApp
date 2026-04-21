package com.sagar.todoapp.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sagar.todoapp.data.local.TodoDao
import com.sagar.todoapp.data.local.TodoEntity
import com.sagar.todoapp.data.remote.TodoApi
import com.sagar.todoapp.data.remote.dto.TodoDto
import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.util.MainCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class TodoRepositoryImplTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val api: TodoApi = mockk()
    private val dao: TodoDao = mockk()
    private lateinit var repository: TodoRepositoryImpl

    private val fakeTodoEntities = listOf(
        TodoEntity(id = 1, title = "Buy groceries", isCompleted = false),
        TodoEntity(id = 2, title = "Walk the dog", isCompleted = true)
    )

    private val fakeTodoDtos = listOf(
        TodoDto(id = 1, title = "Buy groceries", completed = false),
        TodoDto(id = 2, title = "Walk the dog", completed = true)
    )

    private val fakeTodos = listOf(
        Todo(id = 1, title = "Buy groceries", isCompleted = false),
        Todo(id = 2, title = "Walk the dog", isCompleted = true)
    )

    @Before
    fun setup() {
        repository = TodoRepositoryImpl(api, dao)
    }

    // ─────────────────────────────────────────
    // getTodos
    // ─────────────────────────────────────────

    @Test
    fun `getTodos emits todos from local database`() = runTest {
        every { dao.getAllTodos() } returns flowOf(fakeTodoEntities)

        repository.getTodos().test {
            val result = awaitItem()
            assertThat(result).isEqualTo(fakeTodos)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTodos emits empty list when database is empty`() = runTest {
        every { dao.getAllTodos() } returns flowOf(emptyList())

        repository.getTodos().test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTodos emits multiple updates when database changes`() = runTest {
        val updatedEntities = listOf(
            TodoEntity(id = 1, title = "Buy groceries", isCompleted = true)
        )
        every { dao.getAllTodos() } returns flow {
            emit(fakeTodoEntities)
            emit(updatedEntities)
        }

        repository.getTodos().test {
            val first = awaitItem()
            val second = awaitItem()
            assertThat(first).isEqualTo(fakeTodos)
            assertThat(second[0].isCompleted).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTodos propagates exception from dao`() = runTest {
        every { dao.getAllTodos() } throws RuntimeException("DB error")

        assertFailsWith<RuntimeException> {
            repository.getTodos().first()
        }
    }

    // ─────────────────────────────────────────
    // syncTodos
    // ─────────────────────────────────────────

    @Test
    fun `syncTodos fetches from API and upserts into Room`() = runTest {
        coEvery { api.getTodos() } returns fakeTodoDtos
        coEvery { dao.upsertAll(any()) } just Runs

        repository.syncTodos()

        coVerify(exactly = 1) { api.getTodos() }
        coVerify(exactly = 1) { dao.upsertAll(fakeTodoEntities) }
    }

    @Test
    fun `syncTodos silently ignores API failure`() = runTest {
        coEvery { api.getTodos() } throws RuntimeException("Network error")

        // Should not throw
        repository.syncTodos()

        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }

    @Test
    fun `syncTodos silently ignores timeout exception`() = runTest {
        coEvery { api.getTodos() } throws java.net.SocketTimeoutException("Timeout")

        // Should not throw
        repository.syncTodos()

        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }

    @Test
    fun `syncTodos handles empty API response`() = runTest {
        coEvery { api.getTodos() } returns emptyList()
        coEvery { dao.upsertAll(any()) } just Runs

        repository.syncTodos()

        coVerify(exactly = 1) { dao.upsertAll(emptyList()) }
    }

    // ─────────────────────────────────────────
    // addTodo
    // ─────────────────────────────────────────

    @Test
    fun `addTodo upserts to Room first then calls API`() = runTest {
        coEvery { dao.upsertTodo(any()) } just Runs
        coEvery { api.createTodo(any()) } returns fakeTodoDtos[0]

        repository.addTodo(fakeTodos[0])

        coVerifyOrder {
            dao.upsertTodo(fakeTodoEntities[0])
            api.createTodo(any())
        }
    }

    @Test
    fun `addTodo still saves to Room when API fails`() = runTest {
        coEvery { dao.upsertTodo(any()) } just Runs
        coEvery { api.createTodo(any()) } throws RuntimeException("Network error")

        repository.addTodo(fakeTodos[0])

        coVerify(exactly = 1) { dao.upsertTodo(fakeTodoEntities[0]) }
    }

    @Test
    fun `addTodo still saves to Room when API times out`() = runTest {
        coEvery { dao.upsertTodo(any()) } just Runs
        coEvery { api.createTodo(any()) } throws java.net.SocketTimeoutException("Timeout")

        repository.addTodo(fakeTodos[0])

        coVerify(exactly = 1) { dao.upsertTodo(fakeTodoEntities[0]) }
    }

    @Test
    fun `addTodo propagates exception when Room fails`() = runTest {
        coEvery { dao.upsertTodo(any()) } throws RuntimeException("DB error")

        assertFailsWith<RuntimeException> {
            repository.addTodo(fakeTodos[0])
        }

        // API should never be called if Room fails
        coVerify(exactly = 0) { api.createTodo(any()) }
    }

    @Test
    fun `addTodo maps domain model to entity correctly`() = runTest {
        val todo = Todo(id = 99, title = "Test Todo", isCompleted = true)
        val expectedEntity = TodoEntity(id = 99, title = "Test Todo", isCompleted = true)
        coEvery { dao.upsertTodo(any()) } just Runs
        coEvery { api.createTodo(any()) } returns fakeTodoDtos[0]

        repository.addTodo(todo)

        coVerify { dao.upsertTodo(expectedEntity) }
    }

    // ─────────────────────────────────────────
    // updateTodo
    // ─────────────────────────────────────────

    @Test
    fun `updateTodo upserts to Room first then calls API`() = runTest {
        coEvery { dao.upsertTodo(any()) } just Runs
        coEvery { api.updateTodo(any(), any()) } returns fakeTodoDtos[0]

        repository.updateTodo(fakeTodos[0])

        coVerifyOrder {
            dao.upsertTodo(fakeTodoEntities[0])
            api.updateTodo(any(), any())
        }
    }

    @Test
    fun `updateTodo still updates Room when API fails`() = runTest {
        coEvery { dao.upsertTodo(any()) } just Runs
        coEvery { api.updateTodo(any(), any()) } throws RuntimeException("Network error")

        repository.updateTodo(fakeTodos[0])

        coVerify(exactly = 1) { dao.upsertTodo(fakeTodoEntities[0]) }
    }

    @Test
    fun `updateTodo sends correct id to API`() = runTest {
        coEvery { dao.upsertTodo(any()) } just Runs
        coEvery { api.updateTodo(any(), any()) } returns fakeTodoDtos[0]

        repository.updateTodo(fakeTodos[0])

        coVerify { api.updateTodo(eq(fakeTodos[0].id), any()) }
    }

    @Test
    fun `updateTodo propagates exception when Room fails`() = runTest {
        coEvery { dao.upsertTodo(any()) } throws RuntimeException("DB error")

        assertFailsWith<RuntimeException> {
            repository.updateTodo(fakeTodos[0])
        }

        coVerify(exactly = 0) { api.updateTodo(any(), any()) }
    }

    @Test
    fun `updateTodo correctly persists completed state`() = runTest {
        val completedTodo = fakeTodos[0].copy(isCompleted = true)
        coEvery { dao.upsertTodo(any()) } just Runs
        coEvery { api.updateTodo(any(), any()) } returns fakeTodoDtos[0]

        repository.updateTodo(completedTodo)

        coVerify { dao.upsertTodo(match { it.isCompleted }) }
    }

    // ─────────────────────────────────────────
    // deleteTodo
    // ─────────────────────────────────────────

    @Test
    fun `deleteTodo deletes from Room first then calls API`() = runTest {
        coEvery { dao.deleteTodo(any()) } just Runs
        coEvery { api.deleteTodo(any()) } just Runs

        repository.deleteTodo(fakeTodos[0])

        coVerifyOrder {
            dao.deleteTodo(fakeTodoEntities[0])
            api.deleteTodo(any())
        }
    }

    @Test
    fun `deleteTodo still deletes from Room when API fails`() = runTest {
        coEvery { dao.deleteTodo(any()) } just Runs
        coEvery { api.deleteTodo(any()) } throws RuntimeException("Network error")

        repository.deleteTodo(fakeTodos[0])

        coVerify(exactly = 1) { dao.deleteTodo(fakeTodoEntities[0]) }
    }

    @Test
    fun `deleteTodo sends correct id to API`() = runTest {
        coEvery { dao.deleteTodo(any()) } just Runs
        coEvery { api.deleteTodo(any()) } just Runs

        repository.deleteTodo(fakeTodos[0])

        coVerify { api.deleteTodo(eq(fakeTodos[0].id)) }
    }

    @Test
    fun `deleteTodo propagates exception when Room fails`() = runTest {
        coEvery { dao.deleteTodo(any()) } throws RuntimeException("DB error")

        assertFailsWith<RuntimeException> {
            repository.deleteTodo(fakeTodos[0])
        }

        coVerify(exactly = 0) { api.deleteTodo(any()) }
    }

    @Test
    fun `deleteTodo maps domain model to entity correctly`() = runTest {
        val todo = Todo(id = 99, title = "Test Todo", isCompleted = false)
        val expectedEntity = TodoEntity(id = 99, title = "Test Todo", isCompleted = false)
        coEvery { dao.deleteTodo(any()) } just Runs
        coEvery { api.deleteTodo(any()) } just Runs

        repository.deleteTodo(todo)

        coVerify { dao.deleteTodo(expectedEntity) }
    }
}