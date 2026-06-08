package com.taskflow.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taskflow.app.R
import com.taskflow.app.ui.common.theme.Black
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Border
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Soft
import com.taskflow.app.ui.common.util.displayDate
import com.taskflow.app.ui.common.util.toDateInput

@Composable
internal fun SearchField(
    placeholder: String,
    value: String = "",
    onValueChange: (String) -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics { contentDescription = placeholder },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, stringResource(R.string.cd_search)) },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Border, unfocusedBorderColor = Border)
    )
}

@Composable
internal fun Field(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1,
    enabled: Boolean = true,
    placeholder: String = "",
    onValueChange: (String) -> Unit = {}
) {
    Column(modifier) {
        Label(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            enabled = enabled,
            placeholder = {
                if (placeholder.isNotBlank()) Text(placeholder, color = Muted, style = MaterialTheme.typography.bodySmall)
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Border, unfocusedBorderColor = Border)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerField(
    label: String,
    value: Long?,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var showPicker by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = value)

    Column(modifier) {
        Label(label)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, Border, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable { showPicker = true }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.toDateInput(),
                color = if (value == null) Muted else Color.Black,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateSelected(pickerState.selectedDateMillis)
                        showPicker = false
                    }
                ) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
internal fun ProfileField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    placeholder: String = "",
    onValueChange: (String) -> Unit = {}
) {
    Column(modifier) {
        Label(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            enabled = enabled,
            placeholder = {
                Text(placeholder, color = Muted, style = MaterialTheme.typography.bodySmall)
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Border,
                unfocusedBorderColor = Border,
                disabledBorderColor = Border,
                disabledContainerColor = Soft,
                disabledTextColor = Color.Black
            )
        )
    }
}

@Composable
internal fun Label(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(4.dp))
}

@Composable
internal fun MiniSelect(text: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    Box(modifier.height(38.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
        Text(text, color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun DropdownSelector(
    label: String,
    selectedText: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    helperText: String = "",
    menuContent: @Composable ColumnScope.() -> Unit
) {
    var expanded by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    Column(modifier) {
        Label(label)
        Box {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .semantics { contentDescription = label }
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(selectedText, color = Color.Black, style = MaterialTheme.typography.bodySmall)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                Column {
                    menuContent()
                }
            }
        }
        if (helperText.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(helperText, color = Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
internal fun ProjectFilterDropdown(
    selectedText: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    menuContent: @Composable ColumnScope.() -> Unit
) {
    var expanded by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .semantics { contentDescription = selectedText }
                .clickable { expanded = true }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selectedText, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = Color.Black, maxLines = 1)
            Icon(Icons.Default.KeyboardArrowDown, stringResource(R.string.cd_expand_dropdown), modifier = Modifier.size(18.dp), tint = Muted)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column {
                menuContent()
            }
        }
    }
}
