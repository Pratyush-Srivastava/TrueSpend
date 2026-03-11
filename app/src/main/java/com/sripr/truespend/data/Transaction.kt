package com.sripr.truespend.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = MonthlyUpload::class,
            parentColumns = ["uploadId"],
            childColumns = ["uploadId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @PrimaryKey val id: String,
    val dateStr: String,
    val narration: String,
    val original_val: Double,
    val current_val: Double,
    val is_manual: Boolean,
    val timestamp: Long,
    @ColumnInfo(index = true) val uploadId: Long
)
