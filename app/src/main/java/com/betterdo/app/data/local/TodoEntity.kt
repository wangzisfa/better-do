package com.betterdo.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted todo. Subtasks and comments are stored as JSON blobs (serialized with
 * kotlinx.serialization) rather than separate tables — simpler and plenty for v1.
 */
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val note: String?,
    val time: String?,
    val tag: String,
    val icon: String,
    val done: Boolean,
    val priority: Boolean,
    val streak: Int,
    val source: String,
    val sourceNote: String?,
    val createdAt: Long,
    val position: Int,
    val subtasksJson: String,
    val commentsJson: String,
)
