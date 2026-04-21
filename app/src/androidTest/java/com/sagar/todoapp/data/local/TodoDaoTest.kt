package com.sagar.todoapp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TodoDaoTest {

    private lateinit var database: TodoDatabase
    private lateinit var dao: TodoDao

    private val fakeTodoEntities = listOf(
        TodoEntity(id = 1, title = "Buy groceries", isCompleted = false),
        TodoEntity(id = 2, title = "Walk the dog", isCompleted = true),
        TodoEntity(id = 3, title = "Read a book", isCompleted = false)
    )

    @Before
    fun setup() {
        // In-memory database — wiped after each test
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TodoDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.todoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ─────────────────────────────────────────
    // upsertAll
    // ─────────────────────────────────────────

    @Test
    fun upsertAll_insertsAllTodos() = runTest {
        dao.upsertAll(fakeTodoEntities)

        val result = dao.getAllTodos().first()
        assertThat(result).hasSize(3)
    }

    @Test
    fun upsertAll_replacesExistingTodos() = runTest {
        dao.upsertAll(fakeTodoEntities)

        val updated = listOf(
            TodoEntity(id = 1, title = "Updated title", isCompleted = true)
        )
        dao.upsertAll(updated)

        val result = dao.getAllTodos().first()
        val todo = result.find { it.id == 1 }
        assertThat(todo?.title).isEqualTo("Updated title")
        assertThat(todo?.isCompleted).isTrue()
    }

    @Test
    fun upsertAll_withEmptyList_doesNothing() = runTest {
        dao.upsertAll(emptyList())

        val result = dao.getAllTodos().first()
        assertThat(result).isEmpty()
    }

    // ─────────────────────────────────────────
    // upsertTodo
    // ─────────────────────────────────────────

    @Test
    fun upsertTodo_insertsSingleTodo() = runTest {
        dao.upsertTodo(fakeTodoEntities[0])

        val result = dao.getAllTodos().first()
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(fakeTodoEntities[0])
    }

    @Test
    fun upsertTodo_updatesExistingTodo() = runTest {
        dao.upsertTodo(fakeTodoEntities[0])

        val updated = fakeTodoEntities[0].copy(title = "Updated", isCompleted = true)
        dao.upsertTodo(updated)

        val result = dao.getAllTodos().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Updated")
        assertThat(result[0].isCompleted).isTrue()
    }

    @Test
    fun upsertTodo_preservesOtherTodosWhenUpdating() = runTest {
        dao.upsertAll(fakeTodoEntities)

        val updated = fakeTodoEntities[0].copy(title = "Updated")
        dao.upsertTodo(updated)

        val result = dao.getAllTodos().first()
        assertThat(result).hasSize(3)
    }

    @Test
    fun upsertTodo_correctlyPersistsCompletedState() = runTest {
        val completedTodo = TodoEntity(id = 10, title = "Done task", isCompleted = true)
        dao.upsertTodo(completedTodo)

        val result = dao.getAllTodos().first()
        assertThat(result[0].isCompleted).isTrue()
    }

    // ─────────────────────────────────────────
    // deleteTodo
    // ─────────────────────────────────────────

    @Test
    fun deleteTodo_removesCorrectTodo() = runTest {
        dao.upsertAll(fakeTodoEntities)

        dao.deleteTodo(fakeTodoEntities[0])

        val result = dao.getAllTodos().first()
        assertThat(result).hasSize(2)
        assertThat(result.none { it.id == 1 }).isTrue()
    }

    @Test
    fun deleteTodo_doesNotAffectOtherTodos() = runTest {
        dao.upsertAll(fakeTodoEntities)

        dao.deleteTodo(fakeTodoEntities[0])

        val result = dao.getAllTodos().first()
        assertThat(result.map { it.id }).containsExactly(2, 3)
    }

    @Test
    fun deleteTodo_onNonExistentTodo_doesNothing() = runTest {
        dao.upsertAll(fakeTodoEntities)

        val nonExistent = TodoEntity(id = 999, title = "Ghost", isCompleted = false)
        dao.deleteTodo(nonExistent)

        val result = dao.getAllTodos().first()
        assertThat(result).hasSize(3)
    }

    @Test
    fun deleteTodo_onEmptyDatabase_doesNothing() = runTest {
        dao.deleteTodo(fakeTodoEntities[0])

        val result = dao.getAllTodos().first()
        assertThat(result).isEmpty()
    }

    // ─────────────────────────────────────────
    // getAllTodos
    // ─────────────────────────────────────────

    @Test
    fun getAllTodos_returnsEmptyListInitially() = runTest {
        val result = dao.getAllTodos().first()
        assertThat(result).isEmpty()
    }

    @Test
    fun getAllTodos_emitsUpdatedListAfterInsert() = runTest {
        dao.getAllTodos().test {
            // Initial empty
            assertThat(awaitItem()).isEmpty()

            // Insert and observe update
            dao.upsertTodo(fakeTodoEntities[0])
            assertThat(awaitItem()).hasSize(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllTodos_emitsUpdatedListAfterDelete() = runTest {
        dao.upsertAll(fakeTodoEntities)

        dao.getAllTodos().test {
            assertThat(awaitItem()).hasSize(3)

            dao.deleteTodo(fakeTodoEntities[0])
            assertThat(awaitItem()).hasSize(2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllTodos_emitsUpdatedListAfterUpsert() = runTest {
        dao.upsertAll(fakeTodoEntities)

        dao.getAllTodos().test {
            assertThat(awaitItem()).hasSize(3)

            val updated = fakeTodoEntities[0].copy(title = "Updated")
            dao.upsertTodo(updated)

            val emission = awaitItem()
            assertThat(emission.find { it.id == 1 }?.title).isEqualTo("Updated")

            cancelAndIgnoreRemainingEvents()
        }
    }
}