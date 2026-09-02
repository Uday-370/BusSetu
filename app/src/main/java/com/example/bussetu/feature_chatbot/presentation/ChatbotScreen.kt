package com.example.bussetu.feature_chatbot.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bussetu.feature_chatbot.domain.model.ChatMessage
import kotlinx.coroutines.launch

// ─── Brand colours ─────────────────────────────────────────────────────────────
private val BotBubbleBg     = Color(0xFFEEF2FF)
private val BotBubbleText   = Color(0xFF1E3A8A)
private val UserBubbleBg    = Color(0xFF1D4ED8)
private val InputBg         = Color(0xFFF1F5F9)
private val SurfaceBg       = Color(0xFFF8FAFF)
private val HeaderGradStart = Color(0xFF1D4ED8)
private val HeaderGradEnd   = Color(0xFF6366F1)

// ─── Screen entry point ────────────────────────────────────────────────────────
@Composable
fun ChatbotScreen(
    onBackClick: () -> Unit,
    viewModel: ChatbotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    // Auto-scroll to the bottom whenever the messages list changes
    LaunchedEffect(uiState.messages.size, uiState.isTyping) {
        if (uiState.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(
                    index = if (uiState.isTyping) uiState.messages.size else uiState.messages.lastIndex
                )
            }
        }
    }

    val onSend = {
        if (inputText.isNotBlank()) {
            viewModel.sendMessage(inputText.trim())
            inputText = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        // ── Header ──────────────────────────────────────────────────────────────
        ChatHeader(onBackClick = onBackClick)

        // ── Quick-reply chips ───────────────────────────────────────────────────
        QuickRepliesRow { chip ->
            viewModel.sendMessage(chip)
        }

        // ── Messages ────────────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    MessageBubble(message = message)
                }
            }

            // Typing indicator
            if (uiState.isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }

        // ── Input bar ───────────────────────────────────────────────────────────
        ChatInputBar(
            value       = inputText,
            onValueChange = { inputText = it },
            onSend      = onSend
        )
    }
}

// ─── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun ChatHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(HeaderGradStart, HeaderGradEnd))
            )
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 14.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bot avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "BusSetu Assistant",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(Color(0xFF4ADE80), CircleShape)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Online",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ─── Quick-reply chips ──────────────────────────────────────────────────────────
private val quickReplies = listOf("routes", "active buses", "help")

@Composable
private fun QuickRepliesRow(onChipClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickReplies.forEach { label ->
            SuggestionChip(
                onClick = { onChipClick(label) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UserBubbleBg
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = BotBubbleBg
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = Color(0xFFBFDBFE)
                )
            )
        }
    }
}

// ─── Message bubble ─────────────────────────────────────────────────────────────
@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            // Bot avatar dot
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(HeaderGradStart, HeaderGradEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(2.dp, RoundedCornerShape(
                    topStart = if (isUser) 18.dp else 4.dp,
                    topEnd   = if (isUser) 4.dp  else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd   = 18.dp
                ))
                .background(
                    color = if (isUser) UserBubbleBg else BotBubbleBg,
                    shape = RoundedCornerShape(
                        topStart = if (isUser) 18.dp else 4.dp,
                        topEnd   = if (isUser) 4.dp  else 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd   = 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            MarkdownText(
                text    = message.text,
                color   = if (isUser) Color.White else BotBubbleText,
                fontSize = 14.sp
            )
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Typing indicator (three bouncing dots) ─────────────────────────────────────
@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(HeaderGradStart, HeaderGradEnd))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DirectionsBus, null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(BotBubbleBg, RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (0..2).forEach { i ->
                    val offsetY by infiniteTransition.animateFloat(
                        initialValue = 0f, targetValue = -6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, delayMillis = i * 120, easing = EaseInOut),
                            repeatMode = RepeatMode.Reverse
                        ), label = "dot$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(y = offsetY.dp)
                            .background(BotBubbleText.copy(alpha = 0.6f), CircleShape)
                    )
                }
            }
        }
    }
}

// ─── Input bar ──────────────────────────────────────────────────────────────────
@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Ask me anything…",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                },
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = InputBg,
                    unfocusedContainerColor = InputBg,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor  = Color.Transparent,
                    cursorColor             = UserBubbleBg
                ),
                shape = RoundedCornerShape(24.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            Spacer(Modifier.width(8.dp))

            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank(),
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = UserBubbleBg,
                    disabledContainerColor = Color(0xFFCBD5E1)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Minimal markdown text renderer ─────────────────────────────────────────────
@Composable
private fun MarkdownText(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val lines = text.split("\n")
    Column {
        lines.forEachIndexed { idx, line ->
            // Skip rendering spacer for empty lines — prevents huge gaps in bot messages
            if (line.isBlank()) {
                if (idx > 0 && idx < lines.lastIndex) {
                    Spacer(Modifier.height(4.dp)) // single small gap for paragraph breaks
                }
                return@forEachIndexed
            }
            if (idx > 0) Spacer(Modifier.height(2.dp))
            val parts = parseBoldSegments(line)
            Row {
                parts.forEach { (segment, isBold) ->
                    Text(
                        text = segment,
                        color = color,
                        fontSize = fontSize,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = (fontSize.value * 1.45f).sp
                    )
                }
            }
        }
    }
}

private fun parseBoldSegments(line: String): List<Pair<String, Boolean>> {
    val result = mutableListOf<Pair<String, Boolean>>()
    var remaining = line
    while (remaining.contains("**")) {
        val start = remaining.indexOf("**")
        val end   = remaining.indexOf("**", start + 2)
        if (end == -1) break
        if (start > 0) result.add(remaining.substring(0, start) to false)
        result.add(remaining.substring(start + 2, end) to true)
        remaining = remaining.substring(end + 2)
    }
    if (remaining.isNotEmpty()) result.add(remaining to false)
    return result.ifEmpty { listOf(line to false) }
}
