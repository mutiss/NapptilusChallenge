package com.mutissx.napptilusrickandmorty.feature.characters.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.core.ui.theme.NapptilusRickAndMortyTheme

private const val DISABLED_ALPHA = 0.38f

@Composable
fun FilterChipsRow(
    selectedStatus: CharacterStatus?,
    selectedGender: CharacterGender?,
    onStatusSelected: (CharacterStatus) -> Unit,
    onGenderSelected: (CharacterGender) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CharacterStatus.entries, key = { "status-${it.name}" }) { status ->
            AppFilterChip(
                label = stringResource(status.labelRes),
                selected = status == selectedStatus,
                onClick = { onStatusSelected(status) },
                enabled = enabled,
                leadingIcon = { StatusDot(status = status) },
                modifier = Modifier.testTag(TestTags.statusChip(status))
            )
        }
        item(key = "divider") {
            VerticalDivider(
                modifier = Modifier
                    .height(32.dp)
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
        items(CharacterGender.entries, key = { "gender-${it.name}" }) { gender ->
            AppFilterChip(
                label = stringResource(gender.labelRes),
                selected = gender == selectedGender,
                onClick = { onGenderSelected(gender) },
                enabled = enabled,
                modifier = Modifier.testTag(TestTags.genderChip(gender))
            )
        }
    }
}

@Composable
private fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = leadingIcon,
        shape = MaterialTheme.shapes.extraLarge,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            // Keep the active filter recognisable while the row is locked offline.
            disabledSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = DISABLED_ALPHA)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun FilterChipsRowPreview() {
    NapptilusRickAndMortyTheme {
        FilterChipsRow(
            selectedStatus = CharacterStatus.ALIVE,
            selectedGender = null,
            onStatusSelected = {},
            onGenderSelected = {}
        )
    }
}
