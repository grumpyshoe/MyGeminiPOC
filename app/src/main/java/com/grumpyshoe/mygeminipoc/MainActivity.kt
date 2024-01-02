package com.grumpyshoe.mygeminipoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.grumpyshoe.mygeminipoc.ui.theme.MyGeminiPOCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            MyGeminiPOCTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-pro",
                        apiKey = BuildConfig.apiKey
                    )
                    val viewModel = ChatViewModel(generativeModel)
                    ChatRoute(viewModel)
                }
            }
        }
    }
}

@Composable
internal fun ChatRoute(
    chatViewModel: ChatViewModel = viewModel()
) {
    val chatUiState by chatViewModel.uiState.collectAsState()

    ChatScreen(chatUiState, onSendMessage = { inputText ->
        chatViewModel.askGemini(inputText)
    })
}

@Composable
fun ChatScreen(
    uiState: ChatUiState = ChatUiState.Initial,
    onSendMessage: (String) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    var prompt by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .padding(all = 8.dp)
    ) {

        Box(
            modifier = Modifier
                .imePadding()
                .weight(1f)
        ) {

            when (uiState) {
                ChatUiState.Initial -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Face,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                textAlign = TextAlign.Center,
                                text = "Hi, I'm the oracle of delphi.\nAsk me any question and I will give you some advise...",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }

                is ChatUiState.Success -> {

                    LaunchedEffect(uiState) {
                        prompt = ""
                    }

                    LazyColumn {

                        items(uiState.conversation) { item ->

                            when (item.author) {
                                Author.USER -> {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(all = 8.dp)
                                                .background(
                                                    color = item.author.color,
                                                    shape = RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        bottomStart = 16.dp,
                                                        bottomEnd = 16.dp
                                                    )
                                                )
                                                .padding(all = 8.dp)
                                        ) {
                                            Column {

                                                Text(
                                                    text = item.author.displayName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                                Text(
                                                    text = item.message,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                            }
                                            Icon(
                                                item.author.icon,
                                                contentDescription = "User"
                                            )
                                        }
                                    }
                                }

                                Author.GEMINI -> {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(all = 8.dp)
                                                .background(
                                                    color = item.author.color,
                                                    shape = RoundedCornerShape(
                                                        topEnd = 16.dp,
                                                        bottomStart = 16.dp,
                                                        bottomEnd = 16.dp
                                                    )
                                                )
                                                .padding(all = 8.dp)
                                        ) {
                                            Icon(
                                                item.author.icon,
                                                contentDescription = "Gemini"
                                            )
                                            Column {

                                                Text(
                                                    text = item.author.displayName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                                Text(
                                                    text = item.message,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.conversation.lastOrNull()?.author == Author.USER) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }

                is ChatUiState.Error -> {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(all = 8.dp)
                    )
                }
            }
        }


        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = prompt,
                label = { Text("Ask your question") },
                onValueChange = { prompt = it },
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (prompt.isNotBlank()) {
                            onSendMessage(prompt)
                        }
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier
                    .weight(8f)
            )
            Button(
                onClick = {
                    if (prompt.isNotBlank()) {
                        onSendMessage(prompt)
                        focusManager.clearFocus()
                    }
                },

                modifier = Modifier
                    .weight(2f)
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(stringResource(R.string.action_go))
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun ChatScreenPreview() {
    ChatScreen(
        uiState = ChatUiState.Success(
            conversation = listOf(
                UIConversationItem(
                    author = Author.USER,
                    message = "My Test Message"
                ),
                UIConversationItem(
                    author = Author.GEMINI,
                    message = "My Test An vdsgklfsdsjklfdgjklfdsjglfkdsjglkfsdjglfkdsjgklfdsjgklfdjsgklfdjxgklfdjsglkfdjgklfdxjgfkldsswer"
                )
            )
        )
    )
}

@Composable
@Preview(showSystemUi = true)
fun InitPreview() {
    ChatScreen(
        uiState = ChatUiState.Initial
    )
}