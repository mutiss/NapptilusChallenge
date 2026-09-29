package com.mutissx.napptilusrickandmorty.presentation.detail.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags

@Composable
internal fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Translucent disc so the arrow stays visible over any avatar.
    IconButton(
        onClick = onClick,
        modifier = modifier.testTag(TestTags.DETAIL_BACK_BUTTON),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.45f),
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back_button_description)
        )
    }
}
