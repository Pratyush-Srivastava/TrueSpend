package com.sripr.truespend.parser

import com.sripr.truespend.data.Transaction
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

class SmartParser {

    fun parse(inputStream: InputStream): List<Transaction> {
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        
        val transactions = mutableListOf<Transaction>()
        var foundHeader = false
        
        var dateIdx = -1
        var narrationIdx = -1
        var chqRefIdx = -1
        var withdrawalIdx = -1
        var depositIdx = -1

        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        var encounteredEmptyRow = false

        for (row in sheet) {
            if (row == null) continue

            val cells = row.toMutableList()
            if (cells.isEmpty()) continue

            // Check if entirely empty
            if (row.all { it.cellType == org.apache.poi.ss.usermodel.CellType.BLANK || getCellValue(it).isBlank() }) {
                if (foundHeader && transactions.isNotEmpty()) {
                    encounteredEmptyRow = true
                }
                continue
            }
            
            val firstCellVal = getCellValue(row.getCell(0)).trim()
            if (firstCellVal.startsWith("********") && encounteredEmptyRow) {
                // The "Stop" Condition: asterisks encountered AFTER we've finished reading transactions ( indicated by an empty row )
                break
            } else if (firstCellVal.startsWith("********") && !encounteredEmptyRow) {
                // It's just an interleaved asterisk row or a header asterisk row, ignore it.
                continue
            }

            if (!foundHeader) {
                // The Scan
                val rowValues = row.map { getCellValue(it).trim() }
                dateIdx = rowValues.indexOfFirst { it.contains("Date", ignoreCase = true) }
                narrationIdx = rowValues.indexOfFirst { it.contains("Narration", ignoreCase = true) }
                chqRefIdx = rowValues.indexOfFirst { it.contains("Chq./Ref.No.", ignoreCase = true) }
                
                if (dateIdx != -1 && narrationIdx != -1 && chqRefIdx != -1) {
                    withdrawalIdx = rowValues.indexOfFirst { it.contains("Withdrawal Amt", ignoreCase = true) }
                    depositIdx = rowValues.indexOfFirst { it.contains("Deposit Amt", ignoreCase = true) }
                    foundHeader = true
                }
                continue
            }

            // The Loop: Parsing transactions
            val dateStr = getCellValue(row.getCell(dateIdx)).trim()
            val narration = getCellValue(row.getCell(narrationIdx)).trim()
            var chqRef = getCellValue(row.getCell(chqRefIdx)).trim()
            
            val withdrawalStr = getCellValue(row.getCell(withdrawalIdx)).trim().replace(",", "")
            val depositStr = getCellValue(row.getCell(depositIdx)).trim().replace(",", "")

            val withdrawal = withdrawalStr.toDoubleOrNull() ?: 0.0
            val deposit = depositStr.toDoubleOrNull() ?: 0.0

            val originalVal = if (deposit > 0) deposit else -withdrawal

            var timestamp = 0L
            try {
                if (dateStr.isNotEmpty()) {
                    val date = dateFormat.parse(dateStr)
                    if (date != null) {
                        timestamp = date.time
                    }
                }
            } catch (e: Exception) {
                // ignore or handle date parse error
            }

            if (chqRef.isEmpty() || chqRef == "null") {
                chqRef = "EXC_${row.rowNum}_$timestamp"
            }

            // We do a final check to ensure this is a valid transaction row (at least has date or narration)
            if (dateStr.isNotEmpty() || narration.isNotEmpty()) {
                transactions.add(
                    Transaction(
                        id = chqRef,
                        dateStr = dateStr,
                        narration = narration,
                        original_val = originalVal,
                        current_val = originalVal,
                        is_manual = false,
                        timestamp = timestamp,
                        uploadId = 0L
                    )
                )
            }
        }
        
        workbook.close()
        return transactions
    }

    private fun getCellValue(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                // Check if it's a date formatting? HDFC Usually writes dates as strings anyway.
                // But just in case:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    val date = cell.dateCellValue
                    val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    sdf.format(date)
                } else {
                    // avoid scientific notation for large numbers
                    java.math.BigDecimal(cell.numericCellValue).toPlainString()
                }
            }
            org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }
}
