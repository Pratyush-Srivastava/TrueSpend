package com.sripr.truespend

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sripr.truespend.parser.SmartParser
import com.sripr.truespend.ui.DashboardScreen
import com.sripr.truespend.ui.TransactionListScreen
import com.sripr.truespend.ui.TransactionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val viewModel: TransactionViewModel by viewModels()
    private var pendingTransactions by mutableStateOf<List<com.sripr.truespend.data.Transaction>?>(null)
    private var currentUpload by mutableStateOf<com.sripr.truespend.data.MonthlyUpload?>(null)

    private val selectExcelLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            parseExcel(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentUpload == null) {
                        DashboardScreen(
                            viewModel = viewModel,
                            pendingTransactions = pendingTransactions,
                            onPendingHandled = { pendingTransactions = null },
                            onImportClick = {
                                selectExcelLauncher.launch("application/vnd.ms-excel")
                            },
                            onUploadClick = { upload ->
                                currentUpload = upload
                            }
                        )
                    } else {
                        TransactionListScreen(
                            uploadId = currentUpload!!.uploadId,
                            uploadName = currentUpload!!.uploadName,
                            viewModel = viewModel,
                            onBack = { currentUpload = null }
                        )
                    }
                }
            }
        }
    }

    private fun parseExcel(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val parser = SmartParser()
                    val transactions = parser.parse(inputStream)
                    
                    withContext(Dispatchers.Main) {
                        pendingTransactions = transactions
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to parse: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

