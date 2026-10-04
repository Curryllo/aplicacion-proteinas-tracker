package com.proteintracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.proteintracker.R
import com.proteintracker.data.local.ProteinEntry
import com.proteintracker.domain.ProteinMath
import com.proteintracker.domain.formatGrams
import com.proteintracker.domain.parseDecimal
import com.proteintracker.ui.components.FoodIconPicker

@Composable
fun LogFoodScreen(
    editing: ProteinEntry?,
    onSave: (name: String, grams: Double, proteinPer100g: Double, iconKey: String) -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Remounting the form when the edited entry changes keeps the fields in sync
    // without a pile of LaunchedEffect bookkeeping.
    key(editing?.id ?: 0L) {
        LogFoodForm(
            editing = editing,
            onSave = onSave,
            onCancelEdit = onCancelEdit,
            modifier = modifier,
        )
    }
}

@Composable
private fun LogFoodForm(
    editing: ProteinEntry?,
    onSave: (name: String, grams: Double, proteinPer100g: Double, iconKey: String) -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable(editing?.id) { mutableStateOf(editing?.name.orEmpty()) }
    var amountText by rememberSaveable(editing?.id) {
        mutableStateOf(editing?.amountGrams?.let { formatGrams(it, 0).replace(',', '.') }.orEmpty())
    }
    var proteinText by rememberSaveable(editing?.id) {
        mutableStateOf(
            editing?.proteinPer100g?.let { formatGrams(it).replace(',', '.') }.orEmpty(),
        )
    }
    // No default: picking an icon is part of logging a food.
    var iconKey by rememberSaveable(editing?.id) { mutableStateOf(editing?.iconKey) }

    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var proteinError by remember { mutableStateOf(false) }
    var iconError by remember { mutableStateOf(false) }

    val amount = parseDecimal(amountText)
    val proteinPer100g = parseDecimal(proteinText)

    val preview = remember(amount, proteinPer100g) {
        if (amount != null && proteinPer100g != null && amount > 0.0 && proteinPer100g >= 0.0) {
            ProteinMath.totalProtein(amount, proteinPer100g)
        } else {
            null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(if (editing == null) R.string.log_title else R.string.log_edit_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (it.isNotBlank()) nameError = false
            },
            label = { Text(stringResource(R.string.field_name)) },
            singleLine = true,
            isError = nameError,
            supportingText = if (nameError) {
                { Text(stringResource(R.string.error_name)) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    if (parseDecimal(it) != null) amountError = false
                },
                label = { Text(stringResource(R.string.field_amount)) },
                singleLine = true,
                isError = amountError,
                supportingText = if (amountError) {
                    { Text(stringResource(R.string.error_amount)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(0.9f),
            )

            OutlinedTextField(
                value = proteinText,
                onValueChange = {
                    proteinText = it
                    if (parseDecimal(it) != null) proteinError = false
                },
                label = { Text(stringResource(R.string.field_protein)) },
                singleLine = true,
                isError = proteinError,
                supportingText = if (proteinError) {
                    { Text(stringResource(R.string.error_protein)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1.1f),
            )
        }

        Spacer(Modifier.height(18.dp))

        PreviewCard(preview = preview)

        Spacer(Modifier.height(20.dp))

        FoodIconPicker(
            selectedKey = iconKey,
            onSelect = {
                iconKey = it.key
                iconError = false
            },
            showError = iconError,
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val validName = name.isNotBlank()
                val validAmount = amount != null && amount > 0.0
                val validProtein = proteinPer100g != null && proteinPer100g >= 0.0
                val validIcon = iconKey != null
                nameError = !validName
                amountError = !validAmount
                proteinError = !validProtein
                iconError = !validIcon
                if (validName && validAmount && validProtein && validIcon) {
                    onSave(name, amount!!, proteinPer100g!!, iconKey!!)

                    if(editing == null){
                        name = ""
                        amountText = ""
                        proteinText = ""
                        iconKey = null
                        nameError = false
                        amountError = false
                        proteinError = false
                        iconError = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (editing == null) R.string.btn_add else R.string.btn_save))
        }

        if (editing != null) {
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = onCancelEdit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.btn_cancel))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PreviewCard(preview: Double?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.preview_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = preview?.let { "${formatGrams(it)} g" }
                        ?: stringResource(R.string.preview_none),
                    style = if (preview != null) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = if (preview != null) Icons.Filled.FitnessCenter else Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f),
                        CircleShape,
                    )
                    .padding(10.dp),
            )
        }
    }
}
