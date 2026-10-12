package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.MessageSender
import com.example.ui.components.ApiKeyWarningBanner
import com.example.ui.components.ChatInputBar
import com.example.ui.components.HistoryDrawerContent
import com.example.ui.components.MessageBubble
import com.example.ui.components.SuggestionsRow
import com.example.ui.components.SuphTopBar
import com.example.ui.components.TypingIndicator
import com.example.ui.theme.Navy950
import com.example.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val inputQuery by viewModel.inputQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Handle back button for closing drawer
    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    // Auto scroll to bottom on new message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Navy950
            ) {
                HistoryDrawerContent(
                    sessions = sessions,
                    currentSessionId = currentSessionId,
                    onSelectSession = { session ->
                        viewModel.loadSession(session)
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(sessionId)
                    },
                    onNewChat = {
                        viewModel.startNewChat()
                    },
                    onClose = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                SuphTopBar(
                    onNewChatClick = { viewModel.startNewChat() },
                    onHistoryClick = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onSettingsClick = onNavigateToSettings,
                    modifier = Modifier.statusBarsPadding()
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    ChatInputBar(
                        value = inputQuery,
                        onValueChange = viewModel::onInputChange,
                        onSend = { viewModel.sendMessage() },
                        isLoading = isLoading
                    )
                }
            },
            containerColor = Navy950,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Navy950),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 768.dp) // Web responsive max-width on wider screens
                ) {
                    if (!viewModel.isApiKeyConfigured()) {
                        ApiKeyWarningBanner(
                            onConfigureClick = onNavigateToSettings
                        )
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                onRetryClick = { viewModel.retryLastMessage() }
                            )
                        }

                        if (isLoading) {
                            item {
                                TypingIndicator()
                            }
                        }

                        // Show suggestion chips when conversation is fresh (only 1 AI welcome message)
                        if (messages.size <= 1 && !isLoading) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                SuggestionsRow(
                                    onSuggestionClick = { prompt ->
                                        viewModel.sendMessage(overridePrompt = prompt)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
