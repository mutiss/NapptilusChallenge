package com.mutissx.napptilusrickandmorty.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mutissx.napptilusrickandmorty.core.ui.theme.NapptilusRickAndMortyTheme

@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ScreenTitlePreview() {
    NapptilusRickAndMortyTheme {
        ScreenTitle(title = "Screen title")
    }
}
