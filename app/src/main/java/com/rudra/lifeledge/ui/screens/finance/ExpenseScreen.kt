package com.rudra.lifeledge.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rudra.lifeledge.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ExpenseUiState(
    val amount: String = "",
    val selectedCategory: ExpenseCategory? = null,
    val description: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val isRecurring: Boolean = false,
    val recurringFrequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

enum class ExpenseCategory(val title: String, val icon: ImageVector, val color: Color) {
    FOOD("Food & Dining", Icons.Default.Restaurant, Color(0xFFEF4444)),
    TRANSPORT("Transport", Icons.Default.DirectionsCar, Color(0xFF3B82F6)),
    SHOPPING("Shopping", Icons.Default.ShoppingBag, Color(0xFFEC4899)),
    ENTERTAINMENT("Entertainment", Icons.Default.Movie, Color(0xFF8B5CF6)),
    BILLS("Bills & Utilities", Icons.Default.Receipt, Color(0xFFF59E0B)),
    HEALTH("Health", Icons.Default.LocalHospital, Color(0xFF22C55E)),
    EDUCATION("Education", Icons.Default.School, Color(0xFF06B6D4)),
    PERSONAL("Personal Care", Icons.Default.Spa, Color(0xFFF97316)),
    GIFT("Gifts & Donations", Icons.Default.CardGiftcard, Color(0xFFE11D48)),
    OTHER("Other", Icons.Default.MoreHoriz, Color(0xFF6B7280))
}

enum class PaymentMethod(val title: String, val icon: ImageVector) {
    CASH("Cash", Icons.Default.AccountBalanceWallet),
    BANK("Bank Transfer", Icons.Default.AccountBalance),
    MOBILE_BANKING("Mobile Banking", Icons.Default.PhoneAndroid),
    CREDIT_CARD("Credit Card", Icons.Default.CreditCard),
    DEBIT_CARD("Debit Card", Icons.Default.CreditScore)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel = remember { ExpenseViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ExpenseAmountCard(
                    amount = uiState.amount,
                    onAmountChange = { viewModel.updateAmount(it) }
                )
            }

            item {
                CategorySelectionCard(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.updateCategory(it) }
                )
            }

            item {
                PaymentMethodCard(
                    selectedMethod = uiState.paymentMethod,
                    onMethodSelected = { viewModel.updatePaymentMethod(it) }
                )
            }

            item {
                ExpenseDescriptionCard(
                    description = uiState.description,
                    onDescriptionChange = { viewModel.updateDescription(it) }
                )
            }

            item {
                ExpenseDateCard(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.updateDate(it) }
                )
            }

            item {
                ExpenseRecurringCard(
                    isRecurring = uiState.isRecurring,
                    frequency = uiState.recurringFrequency,
                    onToggleRecurring = { viewModel.toggleRecurring() },
                    onFrequencyChange = { viewModel.updateFrequency(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.saveExpense() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uiState.amount.isNotBlank() && uiState.selectedCategory != null
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Expense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ExpenseAmountCard(amount: String, onAmountChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Amount",
                style = MaterialTheme.typography.bodyMedium,
                color = Error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "৳",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Error
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            onAmountChange(newValue)
                        }
                    },
                    modifier = Modifier.width(200.dp),
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    placeholder = {
                        Text(
                            "0",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun CategorySelectionCard(
    selectedCategory: ExpenseCategory?,
    onCategorySelected: (ExpenseCategory) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ExpenseCategory.entries) { category ->
                    ExpenseCategoryChip(
                        category = category,
                        isSelected = selectedCategory == category,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseCategoryChip(
    category: ExpenseCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) category.color.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            category.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun PaymentMethodCard(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Payment Method",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { onMethodSelected(method) },
                        label = { Text(method.title, style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = {
                            Icon(
                                method.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseDescriptionCard(description: String, onDescriptionChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Description (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What did you spend on?") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )
        }
    }
}

@Composable
fun ExpenseDateCard(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { },
                modifier = Modifier
                    .background(Primary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = Primary)
            }
        }
    }
}

@Composable
fun ExpenseRecurringCard(
    isRecurring: Boolean,
    frequency: RecurringFrequency,
    onToggleRecurring: () -> Unit,
    onFrequencyChange: (RecurringFrequency) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Recurring Expense",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Set up automatic tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { onToggleRecurring() },
                    colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                )
            }
            if (isRecurring) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(RecurringFrequency.entries) { freq ->
                        FilterChip(
                            selected = frequency == freq,
                            onClick = { onFrequencyChange(freq) },
                            label = { Text(freq.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

class ExpenseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun updateCategory(category: ExpenseCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun toggleRecurring() {
        _uiState.value = _uiState.value.copy(isRecurring = !_uiState.value.isRecurring)
    }

    fun updateFrequency(frequency: RecurringFrequency) {
        _uiState.value = _uiState.value.copy(recurringFrequency = frequency)
    }

    fun saveExpense() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Save to database here
            _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
        }
    }
}
