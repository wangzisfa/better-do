package com.betterdo.app.ui.screens.quickadd

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.ui.components.DodoAvatar
import com.betterdo.app.ui.components.PrimaryButton
import com.betterdo.app.ui.theme.BdTheme
import com.betterdo.app.ui.theme.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    tone: AgentTone,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    val colors = BdTheme.colors
    val accent = BdTheme.accent
    val sheetState = rememberModalBottomSheetState()
    var text by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DodoAvatar(tone, size = 26.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "写下来，Agent 会接手 →",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = tone.color(),
                )
            }
            Spacer(Modifier.size(14.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = colors.surface2) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp)) {
                    if (text.isEmpty()) {
                        Text("想做点什么？", style = MaterialTheme.typography.bodyLarge, color = colors.inkFaint)
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = colors.ink)),
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.size(16.dp))
            PrimaryButton(
                "加到今天",
                onClick = {
                    val t = text.trim()
                    if (t.isNotEmpty()) onAdd(t)
                },
                enabled = text.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
