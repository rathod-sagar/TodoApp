package com.sagar.todoapp.presentation.todolist

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.presentation.todolist.components.AddEditTodoDialog
import com.sagar.todoapp.presentation.todolist.components.EmptyState
import com.sagar.todoapp.presentation.todolist.components.ProgressHeader
import com.sagar.todoapp.presentation.todolist.components.SwipeToDeleteContainer
import com.sagar.todoapp.presentation.todolist.components.TodoItem
import com.sagar.todoapp.ui.theme.TodoAppTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun TodoListRoot(
    viewModel: TodoListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TodoUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    TodoListScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    uiState: TodoUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (TodoEvent) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var todoToEditId by rememberSaveable { mutableStateOf<Int?>(null) }

    val successState = uiState as? TodoUiState.Success
    val todos = successState?.todos ?: emptyList()
    val completedCount = successState?.completedCount ?: 0
    val progress = successState?.progress ?: 0f
    val currentTodoToEdit = todos.find { it.id == todoToEditId }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "My Todos",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "$completedCount of ${todos.size} completed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = remember { { showDialog = true } },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Todo", modifier = Modifier.size(28.dp))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is TodoUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is TodoUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "😕", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Something went wrong",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is TodoUiState.Success -> {
                    if (todos.isEmpty()) {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                ProgressHeader(
                                    progress = progress
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(todos, key = { it.id }, contentType = { it }) { todo ->
                                SwipeToDeleteContainer(
                                    onDelete = remember(todo) {
                                        { onEvent(TodoEvent.DeleteTodo(todo)) }
                                    }
                                ) {
                                    TodoItem(
                                        todo = todo,
                                        onToggle = remember(todo) {
                                            { onEvent(TodoEvent.ToggleTodo(todo)) }
                                        },
                                        onEdit = remember(todo) {
                                            { todoToEditId = todo.id }
                                        },
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = tween(300),
                                            placementSpec = tween(300),
                                            fadeOutSpec = tween(300)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddEditTodoDialog(
                isEditing = false,
                onConfirm = { title ->
                    onEvent(TodoEvent.AddTodo(title))
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }

        currentTodoToEdit?.let { todo ->
            AddEditTodoDialog(
                initialTitle = todo.title,
                isEditing = true,
                onConfirm = { newTitle ->
                    onEvent(TodoEvent.EditTodo(todo, newTitle))
                    todoToEditId = null
                },
                onDismiss = { todoToEditId = null }
            )
        }
    }
}

@Preview
@Composable
private fun TodoListScreenLoadingPreview() {
    TodoAppTheme {
        TodoListScreen(
            uiState = TodoUiState.Loading,
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun TodoListScreenPreview() {
    TodoAppTheme {
        TodoListScreen(
            uiState = TodoUiState.Success(
                todos = listOf(
                    Todo(id = 1, title = "Buy groceries", isCompleted = false),
                    Todo(id = 2, title = "Walk the dog", isCompleted = true)
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun TodoListScreenErrorPreview() {
    TodoAppTheme {
        TodoListScreen(
            uiState = TodoUiState.Error("Error message!"),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}
