package com.betterdo.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.ui.theme.color

/**
 * "Dodo" — the Agent's avatar. A rounded square in the persona color with a face
 * whose mouth/brows change per tone (gentle smiles, coach is level, savage smirks),
 * matching the design's `Dodo` component. Breathes gently when [active].
 */
@Composable
fun DodoAvatar(
    tone: AgentTone,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    active: Boolean = false,
) {
    val face = Color.White
    val transition = rememberInfiniteTransition(label = "dodo")
    val pulse by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "breathe",
    )
    val scaleValue = if (active) pulse else 1f

    Box(
        modifier
            .size(size)
            .scale(scaleValue)
            .clip(RoundedCornerShape(percent = 30))
            .background(tone.color()),
    ) {
        Canvas(Modifier.size(size)) {
            val s = this.size.width / 100f
            fun p(x: Float, y: Float) = Offset(x * s, y * s)

            if (tone == AgentTone.SAVAGE) {
                drawLine(face, p(32f, 38f), p(44f, 42f), 5f * s, StrokeCap.Round)
                drawLine(face, p(68f, 38f), p(56f, 42f), 5f * s, StrokeCap.Round)
            }

            drawCircle(face, 5.2f * s, p(38f, 50f))
            drawCircle(face, 5.2f * s, p(62f, 50f))

            val mouth = Path()
            when (tone) {
                AgentTone.GENTLE -> {
                    mouth.moveTo(40 * s, 62 * s)
                    mouth.quadraticBezierTo(50 * s, 72 * s, 60 * s, 62 * s)
                }
                AgentTone.COACH -> {
                    mouth.moveTo(40 * s, 64 * s)
                    mouth.lineTo(60 * s, 64 * s)
                }
                AgentTone.SAVAGE -> {
                    mouth.moveTo(39 * s, 66 * s)
                    mouth.quadraticBezierTo(50 * s, 60 * s, 61 * s, 67 * s)
                }
            }
            drawPath(mouth, face, style = Stroke(width = 6f * s, cap = StrokeCap.Round))
        }
    }
}
