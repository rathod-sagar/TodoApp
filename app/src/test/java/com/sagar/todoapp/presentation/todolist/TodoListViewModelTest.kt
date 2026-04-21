package com.sagar.todoapp.presentation.todolist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.domain.usecase.AddTodoUseCase
import com.sagar.todoapp.domain.usecase.DeleteTodoUseCase
import com.sagar.todoapp.domain.usecase.GetTodosUseCase
import com.sagar.todoapp.domain.usecase.SyncTodosUseCase
import com.sagar.todoapp.domain.usecase.UpdateTodoUseCase
import com.sagar.todoapp.util.MainCoroutineRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    // Mocks
    private val getTodosUseCase: GetTodosUseCase = mockk()
    private val syncTodosUseCase: SyncTodosUseCase = mockk()
    private val addTodoUseCase: AddTodoUseCase = mockk()
    private val updateTodoUseCase: UpdateTodoUseCase = mockk()
    private val deleteTodoUseCase: DeleteTodoUseCase = mockk()

    private lateinit var viewModel: TodoListViewModel

    private val fakeTodos = listOf(
        Todo(id = 1, title = "Buy groceries", isCompleted = false),
        Todo(id = 2, title = "Walk the dog", isCompleted = true)
    )

    @Before
    fun setup() {
        coEvery { syncTodosUseCase() } just Runs
        every { getTodosUseCase() } returns flowOf(fakeTodos)

        viewModel = TodoListViewModel(
            getTodosUseCase,
            syncTodosUseCase,
            addTodoUseCase,
            updateTodoUseCase,
            deleteTodoUseCase
        )
    }

    // ─────────────────────────────────────────
    // UiState
    // ─────────────────────────────────────────

    @Test
    fun `uiState is Loading initially`() = runTest {
        every { getTodosUseCase() } returns flow { delay(100); emit(fakeTodos) }

        val vm = TodoListViewModel(
            getTodosUseCase, syncTodosUseCase,
            addTodoUseCase, updateTodoUseCase, deleteTodoUseCase,
        )

        // Collect to trigger onStart
        val job = launch { vm.uiState.collect {} }
        assertThat(vm.uiState.value).isInstanceOf(TodoUiState.Loading::class.java)
        job.cancel()
    }

    @Test
    fun `uiState is Success after todos loaded`() = runTest {
        viewModel.uiState.test {
            // Trigger onStart
            val state = awaitItem()
            assertThat(state).isInstanceOf(TodoUiState.Success::class.java)
            assertThat((state as TodoUiState.Success).todos).isEqualTo(fakeTodos)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState is Error when getTodos throws`() = runTest {
        every { getTodosUseCase() } throws RuntimeException("DB error")

        val vm = TodoListViewModel(
            getTodosUseCase, syncTodosUseCase,
            addTodoUseCase, updateTodoUseCase, deleteTodoUseCase
        )

        vm.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(TodoUiState.Error::class.java)
            assertThat((state as TodoUiState.Error).message).isEqualTo("Failed to load todos.")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState Success contains correct completedCount`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem() as TodoUiState.Success
            assertThat(state.completedCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState Success contains correct progress`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem() as TodoUiState.Success
            assertThat(state.progress).isEqualTo(0.5f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState Success progress is 0 when no todos`() = runTest {
        every { getTodosUseCase() } returns flowOf(emptyList())

        val vm = TodoListViewModel(
            getTodosUseCase, syncTodosUseCase,
            addTodoUseCase, updateTodoUseCase, deleteTodoUseCase,
        )

        vm.uiState.test {
            val state = awaitItem() as TodoUiState.Success
            assertThat(state.progress).isEqualTo(0f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────
    // hasLoadedInitialData
    // ─────────────────────────────────────────

    @Test
    fun `todos are not loaded twice on resubscription`() = runTest {
        // First collection — triggers onStart
        viewModel.uiState.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // Second collection — should NOT trigger onStart again
        viewModel.uiState.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { getTodosUseCase() }
    }

    // ─────────────────────────────────────────
    // onEvent - AddTodo
    // ─────────────────────────────────────────

    @Test
    fun `onEvent AddTodo calls addTodoUseCase with correct data`() = runTest {
        coEvery { addTodoUseCase(any()) } just Runs

        viewModel.onEvent(TodoEvent.AddTodo("New Task"))

        coVerify {
            addTodoUseCase(match { it.title == "New Task" && !it.isCompleted })
        }
    }

    @Test
    fun `onEvent AddTodo trims whitespace from title`() = runTest {
        coEvery { addTodoUseCase(any()) } just Runs

        viewModel.onEvent(TodoEvent.AddTodo("  Buy milk  "))

        coVerify { addTodoUseCase(match { it.title == "Buy milk" }) }
    }

    @Test
    fun `onEvent AddTodo does nothing when title is blank`() = runTest {
        viewModel.onEvent(TodoEvent.AddTodo(""))

        coVerify(exactly = 0) { addTodoUseCase(any()) }
    }

    @Test
    fun `onEvent AddTodo does nothing when title is whitespace only`() = runTest {
        viewModel.onEvent(TodoEvent.AddTodo("    "))

        coVerify(exactly = 0) { addTodoUseCase(any()) }
    }

    @Test
    fun `onEvent AddTodo sends snackbar event when use case throws`() = runTest {
        coEvery { addTodoUseCase(any()) } throws Exception("error")

        viewModel.onEvent(TodoEvent.AddTodo("Failing Task"))
        advanceUntilIdle()

        viewModel.uiEvent.test {
            assertThat(awaitItem()).isInstanceOf(TodoUiEvent.ShowSnackbar::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────
    // onEvent - ToggleTodo
    // ─────────────────────────────────────────

    @Test
    fun `onEvent ToggleTodo marks incomplete todo as completed`() = runTest {
        coEvery { updateTodoUseCase(any()) } just Runs
        val incompleteTodo = fakeTodos[0] // isCompleted = false

        viewModel.onEvent(TodoEvent.ToggleTodo(incompleteTodo))

        coVerify {
            updateTodoUseCase(match { it.id == incompleteTodo.id && it.isCompleted })
        }
    }

    @Test
    fun `onEvent ToggleTodo marks completed todo as incomplete`() = runTest {
        coEvery { updateTodoUseCase(any()) } just Runs
        val completedTodo = fakeTodos[1] // isCompleted = true

        viewModel.onEvent(TodoEvent.ToggleTodo(completedTodo))

        coVerify {
            updateTodoUseCase(match { it.id == completedTodo.id && !it.isCompleted })
        }
    }

    @Test
    fun `onEvent ToggleTodo sends snackbar event when use case throws`() = runTest {
        coEvery { updateTodoUseCase(any()) } throws Exception("error")

        viewModel.onEvent(TodoEvent.ToggleTodo(fakeTodos[0]))
        advanceUntilIdle()

        viewModel.uiEvent.test {
            assertThat(awaitItem()).isInstanceOf(TodoUiEvent.ShowSnackbar::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────
    // onEvent - EditTodo
    // ─────────────────────────────────────────

    @Test
    fun `onEvent EditTodo updates title correctly`() = runTest {
        coEvery { updateTodoUseCase(any()) } just Runs

        viewModel.onEvent(TodoEvent.EditTodo(fakeTodos[0], "Updated Title"))

        coVerify { updateTodoUseCase(match { it.title == "Updated Title" }) }
    }

    @Test
    fun `onEvent EditTodo trims whitespace from new title`() = runTest {
        coEvery { updateTodoUseCase(any()) } just Runs

        viewModel.onEvent(TodoEvent.EditTodo(fakeTodos[0], "  Updated Title  "))

        coVerify { updateTodoUseCase(match { it.title == "Updated Title" }) }
    }

    @Test
    fun `onEvent EditTodo keeps other fields unchanged`() = runTest {
        coEvery { updateTodoUseCase(any()) } just Runs
        val todo = fakeTodos[1] // isCompleted = true

        viewModel.onEvent(TodoEvent.EditTodo(todo, "New Title"))

        coVerify {
            updateTodoUseCase(match {
                it.id == todo.id &&
                        it.title == "New Title" &&
                        it.isCompleted == todo.isCompleted
            })
        }
    }

    @Test
    fun `onEvent EditTodo does nothing when new title is blank`() = runTest {
        viewModel.onEvent(TodoEvent.EditTodo(fakeTodos[0], ""))

        coVerify(exactly = 0) { updateTodoUseCase(any()) }
    }

    @Test
    fun `onEvent EditTodo does nothing when new title is whitespace only`() = runTest {
        viewModel.onEvent(TodoEvent.EditTodo(fakeTodos[0], "   "))

        coVerify(exactly = 0) { updateTodoUseCase(any()) }
    }

    @Test
    fun `onEvent EditTodo sends snackbar event when use case throws`() = runTest {
        coEvery { updateTodoUseCase(any()) } throws Exception("error")

        viewModel.onEvent(TodoEvent.EditTodo(fakeTodos[0], "New Title"))
        advanceUntilIdle()

        viewModel.uiEvent.test {
            assertThat(awaitItem()).isInstanceOf(TodoUiEvent.ShowSnackbar::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────
    // onEvent - DeleteTodo
    // ─────────────────────────────────────────

    @Test
    fun `onEvent DeleteTodo calls deleteTodoUseCase with correct todo`() = runTest {
        coEvery { deleteTodoUseCase(any()) } just Runs

        viewModel.onEvent(TodoEvent.DeleteTodo(fakeTodos[0]))

        coVerify { deleteTodoUseCase(fakeTodos[0]) }
    }

    @Test
    fun `onEvent DeleteTodo sends snackbar event when use case throws`() = runTest {
        coEvery { deleteTodoUseCase(any()) } throws Exception("error")

        viewModel.onEvent(TodoEvent.DeleteTodo(fakeTodos[0]))
        advanceUntilIdle()

        viewModel.uiEvent.test {
            assertThat(awaitItem()).isInstanceOf(TodoUiEvent.ShowSnackbar::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────
    // Loading state
    // ─────────────────────────────────────────

    @Test
    fun `isLoading is false after todos are loaded`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isNotInstanceOf(TodoUiState.Loading::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────
    // Sync
    // ─────────────────────────────────────────

    @Test
    fun `syncTodos is called on init`() = runTest {
        // Trigger onStart by collecting
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        job.cancel()

        coVerify(exactly = 1) { syncTodosUseCase() }
    }

    @Test
    fun `syncTodos failure does not affect uiState`() = runTest {
        coEvery { syncTodosUseCase() } throws RuntimeException("Sync failed")

        val vm = TodoListViewModel(
            getTodosUseCase, syncTodosUseCase,
            addTodoUseCase, updateTodoUseCase, deleteTodoUseCase
        )

        vm.uiState.test {
            assertThat(awaitItem()).isInstanceOf(TodoUiState.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}