package com.tripgenie.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.tripgenie.data.models.ChatMessage
import com.tripgenie.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FloatingChatbot(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(16.dp)
        ) {
            // Chat Window
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .width(320.dp)
                        .height(480.dp)
                        .shadow(16.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryButtonGradient)
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🧞", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Concierge Genie",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "AI Travel Assistant",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                IconButton(onClick = { isExpanded = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                        }

                        // Messages
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f).padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { message ->
                                ChatBubble(message = message)
                            }
                            if (isLoading) {
                                item { TypingIndicator() }
                            }
                        }

                        // Input Area
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text("Inquire about anything...", fontSize = 13.sp, color = TextHint)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderLight
                                ),
                                maxLines = 2,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryButtonGradient)
                                    .clickable {
                                        if (inputText.isNotBlank()) {
                                            onSendMessage(inputText.trim())
                                            inputText = ""
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FAB Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PrimaryButtonGradient)
                    .shadow(8.dp, CircleShape)
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Text(if (isExpanded) "✕" else "🧞", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PrimaryButtonGradient),
                contentAlignment = Alignment.Center
            ) {
                Text("🧞", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (message.isUser) 16.dp else 4.dp,
                        topEnd = if (message.isUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(if (message.isUser) PrimaryBlue else Color(0xFFF3F4F6))
                .padding(10.dp, 8.dp)
        ) {
            Text(
                text = message.content,
                fontSize = 13.sp,
                color = if (message.isUser) Color.White else TextPrimary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3F4F6))
            .padding(12.dp, 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(600, delayMillis = index * 200),
                    RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size((8 * scale).dp)
                    .clip(CircleShape)
                    .background(TextSecondary)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatBubblePreview() {
    TripGenieTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ChatBubble(ChatMessage("Hello! How can I help?", isUser = false))
            Spacer(Modifier.height(8.dp))
            ChatBubble(ChatMessage("Tell me about Udupi", isUser = true))
        }
    }
}
