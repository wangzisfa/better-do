package com.betterdo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.ui.theme.BdTheme

@Composable
fun BottomBar(
    currentRoute: String?,
    onToday: () -> Unit,
    onReview: () -> Unit,
    onQuickAdd: () -> Unit,
) {
    val colors = BdTheme.colors
    val accent = BdTheme.accent
    Surface(color = colors.surface, shadowElevation = 12.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 30.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem("今日", TodoIcon.SUN, selected = currentRoute == "today", onClick = onToday)

            Box(
                Modifier.size(46.dp).clip(CircleShape).background(accent).clickable(onClick = onQuickAdd),
                contentAlignment = Alignment.Center,
            ) {
                BdGlyph(Glyph.PLUS, size = 22.dp, tint = Color.White, strokeWidth = 2.6f)
            }

            NavItem("复盘", TodoIcon.CALENDAR, selected = currentRoute == "review", onClick = onReview)
        }
    }
}

@Composable
private fun NavItem(label: String, icon: TodoIcon, selected: Boolean, onClick: () -> Unit) {
    val colors = BdTheme.colors
    val color = if (selected) BdTheme.accent else colors.inkFaint
    Column(
        Modifier.size(width = 64.dp, height = 44.dp).clip(CircleShape).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BdIcon(icon, size = 19.dp, tint = color, strokeWidth = 1.9f)
        Spacer(Modifier.size(3.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
