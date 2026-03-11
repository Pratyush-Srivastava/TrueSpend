package com.sripr.truespend.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sripr.truespend.data.Transaction
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    uploadId: Long,
    uploadName: String,
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val flowStateFlow = remember(uploadId) { viewModel.getTransactionsForUpload(uploadId) }
    val transactions by flowStateFlow.collectAsState()

    val netFlowStateFlow = remember(uploadId) { viewModel.getUploadNetFlow(uploadId) }
    val netFlow by netFlowStateFlow.collectAsState()

    var showManualEntryDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uploadName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showManualEntryDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Manual Entry")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Batch Net Flow", fontSize = 16.sp)
                    val formattedFlow = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(netFlow)
                    Text(
                        text = formattedFlow,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netFlow >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = "Isolated Transactions",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items = transactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { selectedTransaction = transaction }
                    )
                }
            }
        }
    }

    if (selectedTransaction != null) {
        EditTransactionDialog(
            transaction = selectedTransaction!!,
            onDismiss = { selectedTransaction = null },
            onSave = { updatedVal ->
                viewModel.updateTransaction(selectedTransaction!!.copy(current_val = updatedVal))
                selectedTransaction = null
            }
        )
    }

    if (showManualEntryDialog) {
        ManualEntryDialog(
            onDismiss = { showManualEntryDialog = false },
            onSave = { amount, narration ->
                viewModel.addManualTransaction(amount, narration, uploadId)
                showManualEntryDialog = false
            }
        )
    }
}
