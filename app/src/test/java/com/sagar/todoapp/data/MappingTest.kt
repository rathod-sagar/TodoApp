package com.sagar.todoapp.data

import com.google.common.truth.Truth.assertThat
import com.sagar.todoapp.data.local.TodoEntity
import com.sagar.todoapp.data.remote.dto.TodoDto
import com.sagar.todoapp.domain.model.Todo
import com.sagar.todoapp.data.local.toEntity
import org.junit.Test

class MappingTest {

    // ─────────────────────────────────────────
    // TodoDto.toDomain()
    // ─────────────────────────────────────────

    @Test
    fun `TodoDto toDomain maps all fields correctly`() {
        val dto = TodoDto(id = 1, title = "Buy groceries", completed = false)

        val result = dto.toDomain()

        assertThat(result.id).isEqualTo(1)
        assertThat(result.title).isEqualTo("Buy groceries")
        assertThat(result.isCompleted).isFalse()
    }

    @Test
    fun `TodoDto toDomain maps completed true correctly`() {
        val dto = TodoDto(id = 2, title = "Walk the dog", completed = true)

        val result = dto.toDomain()

        assertThat(result.isCompleted).isTrue()
    }

    @Test
    fun `TodoDto toDomain maps empty title correctly`() {
        val dto = TodoDto(id = 3, title = "", completed = false)

        val result = dto.toDomain()

        assertThat(result.title).isEmpty()
    }

    @Test
    fun `TodoDto toDomain maps id zero correctly`() {
        val dto = TodoDto(id = 0, title = "Some task", completed = false)

        val result = dto.toDomain()

        assertThat(result.id).isEqualTo(0)
    }

    @Test
    fun `TodoDto toDomain maps large id correctly`() {
        val dto = TodoDto(id = Int.MAX_VALUE, title = "Task", completed = false)

        val result = dto.toDomain()

        assertThat(result.id).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun `TodoDto toDomain preserves special characters in title`() {
        val dto = TodoDto(id = 1, title = "Buy milk & eggs @ store #1!", completed = false)

        val result = dto.toDomain()

        assertThat(result.title).isEqualTo("Buy milk & eggs @ store #1!")
    }

    // ─────────────────────────────────────────
    // TodoEntity.toDomain()
    // ─────────────────────────────────────────

    @Test
    fun `TodoEntity toDomain maps all fields correctly`() {
        val entity = TodoEntity(id = 1, title = "Buy groceries", isCompleted = false)

        val result = entity.toDomain()

        assertThat(result.id).isEqualTo(1)
        assertThat(result.title).isEqualTo("Buy groceries")
        assertThat(result.isCompleted).isFalse()
    }

    @Test
    fun `TodoEntity toDomain maps completed true correctly`() {
        val entity = TodoEntity(id = 2, title = "Walk the dog", isCompleted = true)

        val result = entity.toDomain()

        assertThat(result.isCompleted).isTrue()
    }

    @Test
    fun `TodoEntity toDomain maps empty title correctly`() {
        val entity = TodoEntity(id = 3, title = "", isCompleted = false)

        val result = entity.toDomain()

        assertThat(result.title).isEmpty()
    }

    @Test
    fun `TodoEntity toDomain maps id zero correctly`() {
        val entity = TodoEntity(id = 0, title = "Some task", isCompleted = false)

        val result = entity.toDomain()

        assertThat(result.id).isEqualTo(0)
    }

    @Test
    fun `TodoEntity toDomain preserves special characters in title`() {
        val entity = TodoEntity(id = 1, title = "Buy milk & eggs @ store #1!", isCompleted = false)

        val result = entity.toDomain()

        assertThat(result.title).isEqualTo("Buy milk & eggs @ store #1!")
    }

    // ─────────────────────────────────────────
    // Todo.toEntity()
    // ─────────────────────────────────────────

    @Test
    fun `Todo toEntity maps all fields correctly`() {
        val todo = Todo(id = 1, title = "Buy groceries", isCompleted = false)

        val result = todo.toEntity()

        assertThat(result.id).isEqualTo(1)
        assertThat(result.title).isEqualTo("Buy groceries")
        assertThat(result.isCompleted).isFalse()
    }

    @Test
    fun `Todo toEntity maps completed true correctly`() {
        val todo = Todo(id = 2, title = "Walk the dog", isCompleted = true)

        val result = todo.toEntity()

        assertThat(result.isCompleted).isTrue()
    }

    @Test
    fun `Todo toEntity maps empty title correctly`() {
        val todo = Todo(id = 3, title = "", isCompleted = false)

        val result = todo.toEntity()

        assertThat(result.title).isEmpty()
    }

    @Test
    fun `Todo toEntity maps id zero correctly`() {
        val todo = Todo(id = 0, title = "Some task", isCompleted = false)

        val result = todo.toEntity()

        assertThat(result.id).isEqualTo(0)
    }

    @Test
    fun `Todo toEntity preserves special characters in title`() {
        val todo = Todo(id = 1, title = "Buy milk & eggs @ store #1!", isCompleted = false)

        val result = todo.toEntity()

        assertThat(result.title).isEqualTo("Buy milk & eggs @ store #1!")
    }

    // ─────────────────────────────────────────
    // Round trip tests
    // ─────────────────────────────────────────

    @Test
    fun `TodoDto toDomain toEntity round trip preserves all fields`() {
        val dto = TodoDto(id = 1, title = "Buy groceries", completed = true)

        val entity = dto.toDomain().toEntity()

        assertThat(entity.id).isEqualTo(dto.id)
        assertThat(entity.title).isEqualTo(dto.title)
        assertThat(entity.isCompleted).isEqualTo(dto.completed)
    }

    @Test
    fun `TodoEntity toDomain toEntity round trip preserves all fields`() {
        val entity = TodoEntity(id = 1, title = "Buy groceries", isCompleted = true)

        val result = entity.toDomain().toEntity()

        assertThat(result).isEqualTo(entity)
    }

    @Test
    fun `Todo toEntity toDomain round trip preserves all fields`() {
        val todo = Todo(id = 1, title = "Buy groceries", isCompleted = true)

        val result = todo.toEntity().toDomain()

        assertThat(result).isEqualTo(todo)
    }
}