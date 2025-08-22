package com.sun.japaneselisteningtrainer.ui.audio.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.japaneselisteningtrainer.R
import kotlin.math.abs

/* ---------- 1) Lyric line: theo theme, không hard-code màu ---------- */
@Composable
private fun LyricLine(
    text: String,
    isCurrent: Boolean,
    lineProgress: Float,  // 0f..1f cho dòng hiện tại
) {
    val cs = MaterialTheme.colorScheme
    val base = cs.onSurface.copy(alpha = 0.70f)     // chữ thường
    val highlight = cs.onSurface                    // chữ đang hát

    val annotated = remember(text, isCurrent, lineProgress) {
        if (!isCurrent || text.isBlank()) {
            buildAnnotatedString { withStyle(SpanStyle(color = base)) { append(text) } }
        } else {
            val p = lineProgress.coerceIn(0f, 1f)
            val cut = (text.length * p).toInt().coerceIn(0, text.length)
            buildAnnotatedString {
                withStyle(SpanStyle(color = highlight, fontWeight = FontWeight.SemiBold)) {
                    append(text.substring(0, cut))
                }
                if (cut < text.length) {
                    withStyle(SpanStyle(color = base)) { append(text.substring(cut)) }
                }
            }
        }
    }

    Text(
        text = annotated,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
}

/* ---------- 2) LyricsBox: container dùng surfaceVariant theo theme ---------- */
@Composable
fun LyricsBox(
    lines: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = if (lines.isBlank()) Arrangement.Center else Arrangement.Top
        ) {
            if (lines.isBlank()) Text(
                text = stringResource(R.string.please_add_transcript),
                style = MaterialTheme.typography.displayMedium
            ) else Text(
                text = lines,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

/* ---------- 3) Helpers giữ nguyên ---------- */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
    )
}

/* Container nhận swipe để toggle transcript (không dính màu) */
@Composable
fun TranscriptContainer(
    visible: Boolean,
    SWIPE_THRESHOLD_PX: Float = 100f,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val thresholdPx = 40f
    Box(
        modifier = modifier.pointerInput(thresholdPx) {
            var toggleTriggered = false
            var totalDx = 0f
            detectHorizontalDragGestures(
                onDragStart = {
                    toggleTriggered = false
                    totalDx = 0f
                },
                onHorizontalDrag = { _, dragAmount ->
                    if (!toggleTriggered) {
                        totalDx += dragAmount
                        if (abs(totalDx) >= thresholdPx) {
                            toggleTriggered = true
                            onToggle()
                        }
                    }
                },
                onDragEnd = { /* no-op */ },
                onDragCancel = { /* no-op */ }
            )
        }
    ) {
        if (visible) content()
    }
}
