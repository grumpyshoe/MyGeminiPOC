package com.grumpyshoe.mygeminipoc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<ChatUiState> = MutableStateFlow(ChatUiState.Initial)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val conversationItems = mutableListOf<UIConversationItem>()

    fun askGemini(inputText: String) {

        val prompt = "Act as the oracle of delphy and give some mystical advise for the question: $inputText"
        conversationItems.apply {
            clear()
            add(
                UIConversationItem(
                    author = Author.USER,
                    message = inputText
                )
            )
        }
        _uiState.tryEmit(ChatUiState.Success(conversationItems))

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                val text = response.text

                conversationItems.add(
                    UIConversationItem(
                        author = Author.GEMINI,
                        message = text ?: "Sorry, I don't know what to say"
                    )
                )

                _uiState.value = ChatUiState.Success(conversationItems)
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}