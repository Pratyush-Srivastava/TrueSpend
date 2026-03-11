package com.sripr.truespend.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SmartParserTest {

    @Test
    fun testParseHDFCStatement() {
        val file = File("C:/Users/sripr/Downloads/Acct_Statement_XXXXXXXX3511_10032026.xls")
        assertTrue("Test file not found at ${file.absolutePath}", file.exists())

        val parser = SmartParser()
        val inputStream = FileInputStream(file)
        
        val transactions = parser.parse(inputStream)
        
        // According to user, transactions are from row 23 to 82. That's 60 transactions.
        // Row 83 is empty and should be ignored.
        // Row 84 has asterisks and should terminate parsing.
        // Row 22 has asterisks which should be ignored because it's before the header.

        assertEquals(60, transactions.size)

        // Validate the first transaction (row 23) isn't null
        val firstTx = transactions.first()
        assertTrue(firstTx.id.isNotEmpty())
        
        // Print first and last transaction for debugging output
        println("First Tx: $firstTx")
        println("Last Tx: ${transactions.last()}")
    }

    @Test
    fun testParseSecondHDFCStatement() {
        val file = File("C:/Users/sripr/Downloads/Acct Statement_3511_11032026_Sept.xls")
        assertTrue("Test file not found at ${file.absolutePath}", file.exists())

        val parser = SmartParser()
        val inputStream = FileInputStream(file)
        
        val transactions = parser.parse(inputStream)
        
        // According to user, transactions are from row 23 to 70. That's 48 transactions.
        assertEquals(48, transactions.size)

        val firstTx = transactions.first()
        println("Second File First Tx: $firstTx")
        println("Second File Last Tx: ${transactions.last()}")
    }

    @Test
    fun testReactiveFlowSimulation() = runBlocking {
        // 1. The underlying Database Table (List of Transactions)
        val databaseTable = mutableListOf<Double>()

        // 2. The DAO Query Flow (Automatically emits new SUM when table changes)
        val roomDaoNetFlow = kotlinx.coroutines.flow.MutableStateFlow(0.0)

        // Helper to simulate Room triggering the query
        fun triggerRoomSumQuery() {
            val newSum = databaseTable.sum()
            roomDaoNetFlow.value = newSum
        }

        println("\n=== TrueSpend Reactive Pipeline Test ===")

        // Give flow time to start
        delay(100)

        // 3. The Jetpack Compose UI Collector
        // This coroutine runs forever, observing the Flow just like the DashboardScreen does
        val uiObservationJob = launch {
            roomDaoNetFlow.collect { newNetFlow ->
                println("[UI THREAD] Dashboard Card automatically updated to: Rs. $newNetFlow")
            }
        }

        delay(100)
        println("\n[EVENT 1] User clicks 'Import XLS' for September...")
        databaseTable.add(5000.0)   // +5000 Salary
        databaseTable.add(-1500.0)  // -1500 Rent
        databaseTable.add(-850.0)   // -850 Groceries
        triggerRoomSumQuery()       // Room detects insertion and re-runs SUM
        delay(500)

        println("\n[EVENT 2] User clicks 'Import XLS' again for a late statement...")
        databaseTable.add(200.0)    // +200 Refund
        triggerRoomSumQuery()       // Room detects insertion and re-runs SUM
        delay(500)

        println("\n[EVENT 3] User edits a transaction! (Changes Rent from 1500 to 2000)")
        // Update the transaction in the DB
        databaseTable[1] = -2000.0  
        triggerRoomSumQuery()       // Room detects UPDATE and re-runs SUM
        delay(500)

        println("\n[EVENT 4] User adds a Manual transaction (+100)")
        databaseTable.add(100.0)
        triggerRoomSumQuery()       // Room detects insertion and re-runs SUM
        delay(1000)

        uiObservationJob.cancel()
        println("Test Complete!\n")
    }
}
