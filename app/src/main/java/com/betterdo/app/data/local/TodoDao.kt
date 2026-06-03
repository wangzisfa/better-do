package com.betterdo.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY position ASC, createdAt ASC")
    fun observeAll(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    fun observe(id: String): Flow<TodoEntity?>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TodoEntity?

    @Query("SELECT COUNT(*) FROM todos")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(entity: TodoEntity)

    @Upsert
    suspend fun upsertAll(entities: List<TodoEntity>)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun delete(id: String)
}
