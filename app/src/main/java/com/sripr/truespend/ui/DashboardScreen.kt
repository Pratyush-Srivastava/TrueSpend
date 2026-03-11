package com.sripr.truespend.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sripr.truespend.data.MonthlyUpload
import com.sripr.truespend.data.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    pendingTransactions: List<Transaction>?,
    onPendingHandled: () -> Unit,
    onImportClick: () -> Unit,
    onUploadClick: (MonthlyUpload) -> Unit
) {
    val uploads by viewModel.allUploads.collectAsState()
    
    // We can filter by current month for the net flow as a simple implementation here
    val calendar = Calendar.getInstance()
    val flowStateFlow = remember(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)) {
        viewModel.getMonthlyNetFlow(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }
    val monthlyNetFlow by flowStateFlow.collectAsState()

    var showNameDialog by remember(pendingTransactions) { mutableStateOf(pendingTransactions != null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TrueSpend Dashboard") },
                actions = {
                    Button(onClick = onImportClick) {
                        Text("Import XLS")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Dashboard Header
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
                    Text("This Month's Net Flow", fontSize = 16.sp)
                    val formattedFlow = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(monthlyNetFlow)
                    Text(
                        text = formattedFlow,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (monthlyNetFlow >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = "Monthly Uploads",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    items = uploads,
                    key = { it.uploadId }
                ) { upload ->
                    UploadItem(
                        upload = upload,
                        viewModel = viewModel,
                        onClick = { onUploadClick(upload) }
                    )
                }
            }
        }
    }

    if (showNameDialog && pendingTransactions != null) {
        NameUploadDialog(
            defaultName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
            onDismiss = {
                showNameDialog = false
                onPendingHandled()
            },
            onSave = { name ->
                viewModel.insertTransactions(name, pendingTransactions)
                showNameDialog = false
                onPendingHandled()
            }
        )
    }
}

@Composable
fun UploadItem(upload: MonthlyUpload, viewModel: TransactionViewModel, onClick: () -> Unit) {
    val netFlowState = remember(upload.uploadId) { viewModel.getUploadNetFlow(upload.uploadId) }
    val netFlow by netFlowState.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = upload.uploadName, fontWeight = FontWeight.SemiBold, maxLines = 1)
                val importDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(upload.uploadTimestamp))
                Text(text = "Imported: $importDate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(netFlow),
                fontWeight = FontWeight.Bold,
                color = if (netFlow >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun NameUploadDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameText by remember { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Name Your Upload") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Upload Batch Name") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nameText.isNotBlank()) {
                        onSave(nameText)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
fun TransactionItem(transaction: Transaction, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.narration, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(text = transaction.dateStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (transaction.original_val != transaction.current_val) {
                    Text(
                        text = "Original: ${transaction.original_val}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Text(
                text = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(transaction.current_val),
                fontWeight = FontWeight.Bold,
                color = if (transaction.current_val >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(transaction.current_val.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column {
                Text("Narration: ${transaction.narration}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newAmount = amountText.toDoubleOrNull()
                    if (newAmount != null) {
                        onSave(newAmount)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ManualEntryDialog(
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var narration by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Manual Entry") },
        text = {
            Column {
                OutlinedTextField(
                    value = narration,
                    onValueChange = { narration = it },
                    label = { Text("Narration/Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (use - for outflow)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && narration.isNotBlank()) {
                        onSave(amount, narration)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
