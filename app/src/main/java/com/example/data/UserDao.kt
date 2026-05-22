package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM reading_logs ORDER BY dateString DESC")
    fun getLogsFlow(): Flow<List<ReadingLog>>

    @Query("SELECT * FROM reading_logs ORDER BY dateString DESC")
    suspend fun getAllLogs(): List<ReadingLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReadingLog)

    @Query("DELETE FROM reading_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    @Query("DELETE FROM reading_logs")
    suspend fun deleteAllLogs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ReadingLog>)

    // ----------------- Reading Goals -----------------

    @Query("SELECT * FROM reading_goals WHERE id = 1")
    fun getGoalFlow(): Flow<ReadingGoal?>

    @Query("SELECT * FROM reading_goals WHERE id = 1")
    suspend fun getGoal(): ReadingGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: ReadingGoal)

    @Update
    suspend fun updateGoal(goal: ReadingGoal)
}
