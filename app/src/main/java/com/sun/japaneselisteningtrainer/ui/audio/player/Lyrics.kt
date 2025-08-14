package com.sun.japaneselisteningtrainer.ui.audio.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

@Composable
private fun LyricLine(
    text: String,
    isCurrent: Boolean,
    lineProgress: Float,            // 0f..1f cho dòng hiện tại
) {
    val base = Color.White.copy(alpha = 0.65f)
    val highlight = Color.White

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

    Text(text = annotated, fontSize = 16.sp, lineHeight = 22.sp)
}
@Composable
fun LyricsBox(
    lines: List<String>,
    currentLineIndex: Int,
    currentLineProgress: Float,
    onSeekToLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Tự cuộn: đưa dòng hiện tại gần giữa viewport
    LaunchedEffect(currentLineIndex) {
        val target = max(currentLineIndex - 3, 0)
        listState.animateScrollToItem(target)
    }

    Surface(
        color = Color(0x18FFFFFF),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(lines) { index, line ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable { onSeekToLine(index) }
                ) {
                    LyricLine(
                        text = line,
                        isCurrent = (index == currentLineIndex),
                        lineProgress = if (index == currentLineIndex) currentLineProgress else 0f
                    )
                }
            }
        }
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
    )
}
@Composable
fun TranscriptContainer(
    visible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Ngưỡng kéo để nhận là 1 lần "toggle"
    val thresholdPx = 40f
    Box(
        modifier = modifier.pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (abs(dragAmount) >= thresholdPx) onToggle()
            var toggleTriggered = false
            detectHorizontalDragGestures(
                onDragStart = {
                    toggleTriggered = false
                },
                onHorizontalDrag = { _, dragAmount ->
                    if (!toggleTriggered && abs(dragAmount) >= thresholdPx) {
                        toggleTriggered = true
                        onToggle()
                    }
                }
            )
        }
    ) {
        if (visible) content()
    }
}

@Composable
fun LyricsScreen(
    // state
    title: String,
    lines: List<String>,
    isPlaying: Boolean,
    progress: Float,
    currentTimeLabel: String,
    totalTimeLabel: String,
    isShuffleOn: Boolean,
    isFavorite: Boolean,
    currentLineIndex: Int,
    currentLineProgress: Float,
    japaneseVisible: Boolean,                  // <— thêm: hiển thị transcript Nhật
    // callbacks
    onBack: () -> Unit,
    onOpenPicker: () -> Unit,
    onEditAudio: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekFinished: () -> Unit = {},
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSeekToLine: (Int) -> Unit,
    onToggleTranscript: () -> Unit,            // <— thêm: toggle bằng swipe
    // style
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .systemBarsPadding()
    ) {
        // Top bar tái dùng từ Player
        PlayerTopBar(
            titleChip = "New",
            onBack = onBack,
            onOpenPicker = onOpenPicker,
            onEditAudio = onEditAudio
        )

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
            )

            // Khung lời + Swipe toggle
            TranscriptContainer(
                visible = japaneseVisible,
                onToggle = onToggleTranscript,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.58f)
            ) {
                LyricsBox(
                    lines = lines,
                    currentLineIndex = currentLineIndex,
                    currentLineProgress = currentLineProgress,
                    onSeekToLine = onSeekToLine,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar (API hiện tại)
            AudioProgressBar(
                progress = progress,
                currentLabel = currentTimeLabel,
                totalLabel = totalTimeLabel,
                onSeek = onSeek,
                modifier = Modifier.padding(horizontal = 8.dp),
                onSeekFinished = onSeekFinished
            )

            Spacer(Modifier.height(8.dp))
        }

        // Thanh điều khiển dưới – dùng TransportBar mới (stateless)
        TransportBar(
            isPlaying = isPlaying,
            isShuffleOn = isShuffleOn,
            isFavorite = isFavorite,
            onToggleShuffle = onToggleShuffle,
            onPrevious = onPrevious,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onToggleFavorite = onToggleFavorite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
    }
}

/* ---------------------------------- Preview -------------------------------- */

@Preview(
    showBackground = true,
    backgroundColor = 0xFF0D1B2A,
    widthDp = 360,
    heightDp = 780
)
@Composable
private fun LyricsPreview() {
    val mock = listOf(
        "会社で女の人と男の人が話しています。",
        "男：男の人はこれからまず何をしますか。",
        "女：今、注文があった商品を箱に入れてるところなんですけど、手伝ってくれる？",
        "男：箱に入れるんですか。",
        "女：それはもうすぐ終わるから、できたのから配送用の宛名シールを貼っていって。",
        "でも、その前に、一応中身が合ってるかどうか確認してもらえる？",
        "女：うん。じゃ、それは中身を確認する前にお願い。"
    )

    LyricsScreen(
        title = "耳から覚える日本語",
        lines = mock,
        isPlaying = true,
        progress = 0.35f,
        currentTimeLabel = "01:15",
        totalTimeLabel = "03:44",
        isShuffleOn = true,
        isFavorite = false,
        currentLineIndex = 2,
        currentLineProgress = 0.55f,
        japaneseVisible = true,
        onBack = {},
        onOpenPicker = {},
        onEditAudio = {},
        onSeek = {},
        onSeekFinished = {},
        onPlayPause = {},
        onNext = {},
        onPrevious = {},
        onToggleShuffle = {},
        onToggleFavorite = {},
        onSeekToLine = {},
        onToggleTranscript = {}
    )
}
