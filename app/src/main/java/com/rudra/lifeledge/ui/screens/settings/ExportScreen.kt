package com.rudra.lifeledge.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.rudra.lifeledge.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

data class ExportUiState(
    val selectedFormat: String = "JSON",
    val exportOptions: List<ExportOption> = defaultExportOptions,
    val isExporting: Boolean = false,
    val lastExportDate: String? = null,
    val message: String? = null
)

data class ExportOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val isSelected: Boolean = false
)

val defaultExportOptions = listOf(
    ExportOption("transactions", "Transactions", "All income & expenses", Icons.Default.Receipt, Color(0xFF3B82F6)),
    ExportOption("habits", "Habits", "Habit tracking data", Icons.Default.CheckCircle, Color(0xFF22C55E)),
    ExportOption("goals", "Goals", "Goals & milestones", Icons.Default.Flag, Color(0xFFF59E0B)),
    ExportOption("journal", "Journal", "Journal entries", Icons.Default.Book, Color(0xFF8B5CF6)),
    ExportOption("work", "Work", "Work sessions & overtime", Icons.Default.Work, Color(0xFF06B6D4)),
    ExportOption("savings", "Savings", "Savings goals & transactions", Icons.Default.Savings, Color(0xFF10B981))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: ExportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            if (message.contains("saved")) {
                // Share file intent would go here
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                FormatSelectionCard(
                    selectedFormat = uiState.selectedFormat,
                    onFormatSelected = { viewModel.selectFormat(it) }
                )
            }

            item {
                DataSelectionCard(
                    options = uiState.exportOptions,
                    onOptionToggle = { viewModel.toggleOption(it) }
                )
            }

            item {
                ExportSummaryCard(
                    selectedCount = uiState.exportOptions.count { it.isSelected },
                    lastExportDate = uiState.lastExportDate
                )
            }

            item {
                ExportButton(
                    isExporting = uiState.isExporting,
                    onExport = { viewModel.exportData(context) }
                )
            }

            if (uiState.message != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.message!!.contains("Success") || uiState.message!!.contains("saved")) 
                                Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (uiState.message!!.contains("Success") || uiState.message!!.contains("saved")) 
                                    Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (uiState.message!!.contains("Success") || uiState.message!!.contains("saved")) 
                                    Success else Error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.message!!,
                                color = if (uiState.message!!.contains("Success") || uiState.message!!.contains("saved")) 
                                    Success else Error
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun FormatSelectionCard(
    selectedFormat: String,
    onFormatSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Export Format",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormatChip(
                    label = "JSON",
                    isSelected = selectedFormat == "JSON",
                    onClick = { onFormatSelected("JSON") },
                    modifier = Modifier.weight(1f)
                )
                FormatChip(
                    label = "CSV",
                    isSelected = selectedFormat == "CSV",
                    onClick = { onFormatSelected("CSV") },
                    modifier = Modifier.weight(1f)
                )
                FormatChip(
                    label = "PDF",
                    isSelected = selectedFormat == "PDF",
                    onClick = { onFormatSelected("PDF") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                when (selectedFormat) {
                    "JSON" -> "Best for data backup and restoration"
                    "CSV" -> "Best for spreadsheet applications"
                    "PDF" -> "Best for printing and sharing"
                    else -> ""
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FormatChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DataSelectionCard(
    options: List<ExportOption>,
    onOptionToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                            .background(Secondary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DataObject,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Select Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = { options.forEach { onOptionToggle(it.id) } }) {
                    Text("Select All")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            options.forEach { option ->
                DataOptionItem(
                    option = option,
                    onToggle = { onOptionToggle(option.id) }
                )
                if (option.id != options.last().id) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun DataOptionItem(
    option: ExportOption,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = option.isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = Primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(option.color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                option.icon,
                contentDescription = null,
                tint = option.color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                option.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                option.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExportSummaryCard(
    selectedCount: Int,
    lastExportDate: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "$selectedCount data types selected",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (lastExportDate != null) {
                    Text(
                        "Last export: $lastExportDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.CloudDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExportButton(
    isExporting: Boolean,
    onExport: () -> Unit
) {
    Button(
        onClick = onExport,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isExporting,
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isExporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Exporting...")
        } else {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

class ExportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun selectFormat(format: String) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun toggleOption(optionId: String) {
        val updatedOptions = _uiState.value.exportOptions.map { option ->
            if (option.id == optionId) {
                option.copy(isSelected = !option.isSelected)
            } else option
        }
        _uiState.value = _uiState.value.copy(exportOptions = updatedOptions)
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, message = null)
            try {
                val selectedData = _uiState.value.exportOptions.filter { it.isSelected }
                if (selectedData.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        message = "Please select at least one data type"
                    )
                    return@launch
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
                val timestamp = dateFormat.format(Date())
                val format = _uiState.value.selectedFormat

                // Create export data
                val exportData = JSONObject()
                selectedData.forEach { option ->
                    val dataArray = JSONArray()
                    // In real implementation, fetch actual data from database
                    dataArray.put(JSONObject().put("sample", "data"))
                    exportData.put(option.id, dataArray)
                }

                // Save file
                val fileName = "lifeledger_export_$timestamp.$format.lowercase()"
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                
                FileWriter(file).use { writer ->
                    when (format) {
                        "JSON" -> writer.write(exportData.toString(2))
                        "CSV" -> writer.write("data\nsample\n")
                        "PDF" -> writer.write("LifeLedger Export\nData: ${selectedData.joinToString { it.title }}")
                    }
                }

                val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    lastExportDate = dateTimeFormat.format(Date()),
                    message = "Export saved: $fileName"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    message = "Export failed: ${e.message}"
                )
            }
        }
    }
}
