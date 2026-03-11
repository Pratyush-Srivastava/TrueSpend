package com.sripr.truespend.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_uploads")
data class MonthlyUpload(
    @PrimaryKey(autoGenerate = true) val uploadId: Long = 0L,
    val uploadName: String,
    val uploadTimestamp: Long
)
