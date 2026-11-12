package com.kirill.physiognomy2.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kirill.physiognomy2.data.PhysiognomyCatalog
import com.kirill.physiognomy2.ui.components.FeatureArt
import com.kirill.physiognomy2.ui.workspace.featureKindFor

@Composable
fun ArtQaScreen(modifier: Modifier = Modifier) {
    val rows = PhysiognomyCatalog.steps.flatMap { step ->
        step.questions.flatMapIndexed { _, question ->
            question.options.mapIndexed { index, option ->
                Row(
                    step = step.number,
                    title = step.title,
                    question = question.title,
                    option = option.label,
                    index = index,
                )
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(rows) { row ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        FeatureArt(
                            kind = featureKindFor(row.step),
                            variant = row.index,
                            label = row.option,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(width = 58.dp, height = 38.dp),
                            strokeWidth = 1.6f,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f),
                    ) {
                        Text(
                            text = "${row.step} · ${row.question} · #${row.index}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = row.option,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

private data class Row(
    val step: Int,
    val title: String,
    val question: String,
    val option: String,
    val index: Int,
)
