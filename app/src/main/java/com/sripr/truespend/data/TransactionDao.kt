package com.sripr.truespend.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT SUM(current_val) FROM transactions WHERE timestamp >= :startOfMonth AND timestamp <= :endOfMonth")
    fun getMonthlyNetFlow(startOfMonth: Long, endOfMonth: Long): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpload(upload: MonthlyUpload): Long
    
    @Query("SELECT * FROM monthly_uploads ORDER BY uploadTimestamp DESC")
    fun getAllUploads(): Flow<List<MonthlyUpload>>
    
    @Query("SELECT * FROM transactions WHERE uploadId = :uploadId ORDER BY timestamp DESC")
    fun getTransactionsForUpload(uploadId: Long): Flow<List<Transaction>>
    
    @Query("SELECT SUM(current_val) FROM transactions WHERE uploadId = :uploadId")
    fun getUploadNetFlow(uploadId: Long): Flow<Double?>
    
    @Query("DELETE FROM monthly_uploads WHERE uploadId = :uploadId")
    suspend fun deleteUpload(uploadId: Long)
}
