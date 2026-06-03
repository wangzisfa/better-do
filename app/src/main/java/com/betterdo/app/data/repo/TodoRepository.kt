package com.betterdo.app.data.repo

import com.betterdo.app.data.local.TodoDao
import com.betterdo.app.data.local.TodoEntity
import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.Subtask
import com.betterdo.app.domain.model.Tag
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.domain.model.TodoSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Single source of truth for todos. Maps Room entities <-> domain models. */
class TodoRepository(private val dao: TodoDao) {

    private val json = Json { ignoreUnknownKeys = true }

    fun observeTodos(): Flow<List<Todo>> = dao.observeAll().map { list -> list.map(::toDomain) }

    fun observeTodo(id: String): Flow<Todo?> = dao.observe(id).map { it?.let(::toDomain) }

    suspend fun getTodo(id: String): Todo? = dao.getById(id)?.let(::toDomain)

    /** Seed the sample day on first launch so the designed experience is visible. */
    suspend fun ensureSeeded() {
        if (dao.count() == 0) {
            val now = System.currentTimeMillis()
            val all = SeedData.seedTodos(now) + SeedData.doneItems(now)
            dao.upsertAll(all.map(::toEntity))
        }
    }

    suspend fun setDone(id: String, done: Boolean) {
        val t = getTodo(id) ?: return
        save(t.copy(done = done))
    }

    suspend fun toggleSubtask(todoId: String, subtaskId: String) {
        val t = getTodo(todoId) ?: return
        save(t.copy(subtasks = t.subtasks.map {
            if (it.id == subtaskId) it.copy(done = !it.done) else it
        }))
    }

    suspend fun appendComment(todoId: String, comment: Comment) {
        val t = getTodo(todoId) ?: return
        save(t.copy(comments = t.comments + comment))
    }

    suspend fun likeComment(todoId: String, commentId: String) {
        val t = getTodo(todoId) ?: return
        save(t.copy(comments = t.comments.map {
            if (it.id == commentId) it.copy(likes = it.likes + 1) else it
        }))
    }

    suspend fun addTodo(todo: Todo) = save(todo)

    suspend fun addAll(todos: List<Todo>) = dao.upsertAll(todos.map(::toEntity))

    suspend fun delete(id: String) = dao.delete(id)

    suspend fun update(todo: Todo) = save(todo)

    private suspend fun save(todo: Todo) = dao.upsert(toEntity(todo))

    // ---- mapping ----
    private fun toEntity(t: Todo) = TodoEntity(
        id = t.id, title = t.title, note = t.note, time = t.time,
        tag = t.tag.name, icon = t.icon.name, done = t.done, priority = t.priority,
        streak = t.streak, source = t.source.name, sourceNote = t.sourceNote,
        createdAt = t.createdAt, position = t.position,
        subtasksJson = json.encodeToString(t.subtasks),
        commentsJson = json.encodeToString(t.comments),
    )

    private fun toDomain(e: TodoEntity) = Todo(
        id = e.id, title = e.title, note = e.note, time = e.time,
        tag = runCatching { Tag.valueOf(e.tag) }.getOrDefault(Tag.LIFE),
        icon = runCatching { TodoIcon.valueOf(e.icon) }.getOrDefault(TodoIcon.SPARKLE),
        done = e.done, priority = e.priority, streak = e.streak,
        source = runCatching { TodoSource.valueOf(e.source) }.getOrDefault(TodoSource.USER),
        sourceNote = e.sourceNote, createdAt = e.createdAt, position = e.position,
        subtasks = runCatching { json.decodeFromString<List<Subtask>>(e.subtasksJson) }.getOrDefault(emptyList()),
        comments = runCatching { json.decodeFromString<List<Comment>>(e.commentsJson) }.getOrDefault(emptyList()),
    )
}
