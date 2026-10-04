package com.proteintracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import com.proteintracker.R
import com.proteintracker.ui.icons.FoodIcon

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodIconPicker(
    selectedKey: String?,
    onSelect: (FoodIcon) -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.label_icon),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FoodIcon.entries.forEach { icon ->
                FoodIconChoice(
                    icon = icon,
                    selected = icon.key == selectedKey,
                    onClick = { onSelect(icon) },
                )
            }
        }
        if (showError) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.error_icon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun FoodIconChoice(
    icon: FoodIcon,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = if (selected) scheme.primaryContainer else Color.Transparent,
        animationSpec = tween(150),
        label = "iconContainer",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) scheme.primary else scheme.outlineVariant,
        animationSpec = tween(150),
        label = "iconBorder",
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .background(container, CircleShape)
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onClick, role = Role.RadioButton)
            .semantics {
                contentDescription = icon.label
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon.imageVector,
            contentDescription = null,
            tint = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
    }
}
