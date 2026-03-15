package com.rudra.lifeledge.ui.screens.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.CardEntity
import com.rudra.lifeledge.data.local.entity.CardType
import com.rudra.lifeledge.data.repository.CardRepository
import com.rudra.lifeledge.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.launch

data class CardsUiState(
    val cards: List<CardEntity> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val editingCard: CardEntity? = null,
    val showAddCard: Boolean = false,
    val recentlyDeletedCard: CardEntity? = null
)

class CardsViewModel(
    private val cardRepository: CardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            cardRepository.getAllCards().collect { cards ->
                _uiState.value = _uiState.value.copy(
                    cards = cards,
                    isLoading = false
                )
            }
        }
        viewModelScope.launch {
            cardRepository.getTotalBalance().collect { balance ->
                _uiState.value = _uiState.value.copy(totalBalance = balance ?: 0.0)
            }
        }
    }

    fun setEditingCard(card: CardEntity?) {
        _uiState.value = _uiState.value.copy(editingCard = card)
    }

    fun setShowAddCard(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddCard = show)
    }

    fun saveCard(name: String, bankName: String, cardType: CardType, balance: Double, color: Long) {
        viewModelScope.launch {
            val card = CardEntity(
                name = name,
                bankName = bankName,
                cardType = cardType,
                balance = balance,
                color = color.toString()
            )
            cardRepository.saveCard(card)
            _uiState.value = _uiState.value.copy(showAddCard = false)
        }
    }

    fun updateCard(card: CardEntity) {
        viewModelScope.launch {
            cardRepository.updateCard(card)
            _uiState.value = _uiState.value.copy(editingCard = null)
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentlyDeletedCard = card)
            cardRepository.deleteCard(card)
        }
    }

    fun undoDeleteCard() {
        viewModelScope.launch {
            _uiState.value.recentlyDeletedCard?.let { card ->
                cardRepository.saveCard(card)
                _uiState.value = _uiState.value.copy(recentlyDeletedCard = null)
            }
        }
    }

    fun clearDeletedCard() {
        _uiState.value = _uiState.value.copy(recentlyDeletedCard = null)
    }

    fun toggleActive(card: CardEntity) {
        viewModelScope.launch {
            cardRepository.setActive(card.id, !card.isActive)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CardsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.recentlyDeletedCard) {
        uiState.recentlyDeletedCard?.let {
            val result = snackbarHostState.showSnackbar(
                message = "Card deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDeleteCard()
            } else {
                viewModel.clearDeletedCard()
            }
        }
    }

    when {
        uiState.showAddCard -> {
            AddEditCardScreen(
                card = null,
                onSave = { name, bank, type, balance, color ->
                    viewModel.saveCard(name, bank, type, balance, color)
                },
                onNavigateBack = { viewModel.setShowAddCard(false) }
            )
        }
        uiState.editingCard != null -> {
            AddEditCardScreen(
                card = uiState.editingCard,
                onSave = { name, bank, type, balance, color ->
                    viewModel.updateCard(
                        uiState.editingCard!!.copy(
                            name = name,
                            bankName = bank,
                            cardType = type,
                            balance = balance,
                            color = color.toString()
                        )
                    )
                },
                onNavigateBack = { viewModel.setEditingCard(null) }
            )
        }
        else -> {
            CardsContent(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onNavigateBack = onNavigateBack,
                onAddCard = { viewModel.setShowAddCard(true) },
                onEditCard = { viewModel.setEditingCard(it) },
                onDeleteCard = { viewModel.deleteCard(it) },
                onToggleActive = { viewModel.toggleActive(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardsContent(
    uiState: CardsUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAddCard: () -> Unit,
    onEditCard: (CardEntity) -> Unit,
    onDeleteCard: (CardEntity) -> Unit,
    onToggleActive: (CardEntity) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cards & Wallets", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total Wallet Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "৳${String.format("%.0f", uiState.totalBalance)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Text(
                    "Your Cards & Wallets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.cards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No cards yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.cards) { card ->
                    CardItem(
                        card = card,
                        onEdit = { onEditCard(card) },
                        onDelete = { onDeleteCard(card) },
                        onToggleActive = { onToggleActive(card) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CardItem(
    card: CardEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val cardColor = try {
        Color(android.graphics.Color.parseColor(card.color))
    } catch (e: Exception) {
        Primary
    }

    val isDebit = card.cardType == CardType.DEBIT_CARD
    val isWallet = card.cardType == CardType.CASH_WALLET || card.cardType == CardType.MOBILE_WALLET

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isActive) cardColor.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(cardColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (card.cardType) {
                                CardType.DEBIT_CARD, CardType.CREDIT_CARD -> Icons.Default.CreditCard
                                CardType.CASH_WALLET -> Icons.Default.AccountBalanceWallet
                                CardType.MOBILE_WALLET -> Icons.Default.PhoneAndroid
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            card.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${card.bankName} • ${card.cardType.name.replace("_", " ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "৳${String.format("%.0f", card.balance)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = cardColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (card.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (card.isActive) Success else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp),
                            tint = Primary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = Error
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to delete \"${card.name}\"?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    card: CardEntity?,
    onSave: (String, String, CardType, Double, Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf(card?.name ?: "") }
    var bankName by remember { mutableStateOf(card?.bankName ?: "") }
    var selectedType by remember { mutableStateOf(card?.cardType ?: CardType.DEBIT_CARD) }
    var balance by remember { mutableStateOf(card?.balance?.toString() ?: "0") }
    var selectedColor by remember { 
        mutableStateOf(
            try { Color(android.graphics.Color.parseColor(card?.color ?: "#3B82F6")) }
            catch (e: Exception) { Primary }
        )
    }

    val colors = listOf(
        Color(0xFF3B82F6), Color(0xFF22C55E), Color(0xFFEF4444),
        Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899),
        Color(0xFF06B6D4), Color(0xFF6366F1)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (card == null) "Add Card" else "Edit Card", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Card Name") },
                    placeholder = { Text("Personal Debit") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank / Institution") },
                    placeholder = { Text("DBBL, bKash, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Text(
                    "Card Type",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.replace("_", " "), style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Initial Balance") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("৳") },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Text(
                    "Card Color",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (selectedColor == color) Modifier.background(
                                        Color.White.copy(alpha = 0.3f),
                                        CircleShape
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val balanceValue = balance.toDoubleOrNull() ?: 0.0
                        val colorValue = selectedColor.toArgb().toLong() and 0xFFFFFFFFL
                        onSave(name, bankName, selectedType, balanceValue, colorValue)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = name.isNotBlank() && bankName.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (card == null) "Add Card" else "Save Changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
