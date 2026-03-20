package com.rudra.lifeledge.ui.screens.loan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.lifeledge.data.local.entity.Loan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanScreen(
    viewModel: LoanViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loans") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Add, contentDescription = "Add Loan")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Loan")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Loan")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.loans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No loans yet",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add a loan to track your EMI payments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.loans) { loan ->
                    LoanCard(
                        loan = loan,
                        onPayEmi = { viewModel.payEmi(loan.id) },
                        onDelete = { viewModel.deleteLoan(loan) }
                    )
                }
            }
        }
        
        // Show error if any
        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Show success message
        uiState.successMessage?.let { message ->
            LaunchedEffect(message) {
                // Show snackbar or handle success
                viewModel.clearSuccess()
            }
        }
    }
    
    if (showCreateDialog) {
        CreateLoanDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun LoanCard(
    loan: Loan,
    onPayEmi: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (loan.totalAmount > 0) {
        ((loan.totalAmount - loan.remainingAmount) / loan.totalAmount).toFloat()
    } else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = loan.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "EMI: ৳${String.format("%.2f", loan.monthlyEMI)} / month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${loan.interestRate}% p.a.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "৳${String.format("%.2f", loan.remainingAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "৳${String.format("%.2f", loan.totalAmount - loan.remainingAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Button(
                onClick = onPayEmi,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pay EMI - ৳${String.format("%.2f", loan.monthlyEMI)}")
            }
        }
    }
}

@Composable
fun CreateLoanDialog(
    viewModel: LoanViewModel,
    onDismiss: () -> Unit
) {
    val createState by viewModel.createState.collectAsState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Loan") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = createState.name,
                    onValueChange = viewModel::updateCreateName,
                    label = { Text("Loan Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = createState.principal,
                    onValueChange = viewModel::updateCreatePrincipal,
                    label = { Text("Principal Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = createState.interestRate,
                    onValueChange = viewModel::updateCreateInterestRate,
                    label = { Text("Interest Rate (%)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = createState.tenureMonths,
                    onValueChange = viewModel::updateCreateTenure,
                    label = { Text("Tenure (months)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = createState.lender,
                    onValueChange = viewModel::updateCreateLender,
                    label = { Text("Lender") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (createState.principal.isNotEmpty() && 
                    createState.interestRate.isNotEmpty() && 
                    createState.tenureMonths.isNotEmpty()) {
                    viewModel.calculateEmi()
                    Text(
                        text = "EMI: ৳${String.format("%.2f", viewModel.uiState.value.calculatedEmi)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.createLoan()
                    onDismiss()
                },
                enabled = createState.name.isNotEmpty() && 
                         createState.principal.isNotEmpty() &&
                         createState.interestRate.isNotEmpty() &&
                         createState.tenureMonths.isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmiScreen(
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: LoanViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMI Payments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Add, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LoanScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}
