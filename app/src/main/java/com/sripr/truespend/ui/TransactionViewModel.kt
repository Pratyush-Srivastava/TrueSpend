package com.sripr.truespend.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sripr.truespend.data.AppDatabase
import com.sripr.truespend.data.MonthlyUpload
import com.sripr.truespend.data.Transaction
import com.sripr.truespend.data.TransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionDao: TransactionDao =
        AppDatabase.getDatabase(application).transactionDao()

    val allUploads: StateFlow<List<MonthlyUpload>> = transactionDao.getAllUploads()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getTransactionsForUpload(uploadId: Long): StateFlow<List<Transaction>> {
        return transactionDao.getTransactionsForUpload(uploadId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun getUploadNetFlow(uploadId: Long): StateFlow<Double> {
        return transactionDao.getUploadNetFlow(uploadId)
            .map { it ?: 0.0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )
    }

    fun getMonthlyNetFlow(year: Int, month: Int): StateFlow<Double> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis
        
        return transactionDao.getMonthlyNetFlow(startOfMonth, endOfMonth)
            .map { it ?: 0.0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )
    }

    fun insertTransactions(uploadName: String, transactions: List<Transaction>) = viewModelScope.launch(Dispatchers.IO) {
        val upload = MonthlyUpload(
            uploadName = uploadName,
            uploadTimestamp = System.currentTimeMillis()
        )
        val uploadId = transactionDao.insertUpload(upload)
        val linkedTransactions = transactions.map { it.copy(uploadId = uploadId) }
        transactionDao.insertAll(linkedTransactions)
    }
    
    fun deleteUpload(uploadId: Long) = viewModelScope.launch(Dispatchers.IO) {
        transactionDao.deleteUpload(uploadId)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        transactionDao.update(transaction)
    }

    fun addManualTransaction(amount: Double, narration: String, uploadId: Long) = viewModelScope.launch(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val transaction = Transaction(
            id = "MAN_$timestamp",
            dateStr = "Manual",
            narration = narration,
            original_val = amount,
            current_val = amount,
            is_manual = true,
            timestamp = timestamp,
            uploadId = uploadId
        )
        transactionDao.insert(transaction)
    }
}
