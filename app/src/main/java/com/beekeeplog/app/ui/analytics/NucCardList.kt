package com.beekeeplog.app.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beekeeplog.app.domain.model.NucWithQueen

/** Scrollable list of [NucCard]s. */
@Composable
fun NucCardList(
    nucs: List<NucWithQueen>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nucs, key = { it.nucId }) { nuc ->
            NucCard(nuc = nuc)
        }
    }
}
