package com.grumpyshoe.mygeminipoc

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface ChatUiState {

    /**
     * Empty state when the screen is first shown
     */
    object Initial : ChatUiState

    /**
     * Still loading
     */
//    object Loading : ChatUiState

    /**
     * Text has been generated
     */
    class Success(
        val conversation: List<UIConversationItem>
    ) : ChatUiState

    /**
     * There was an error generating text
     */
    data class Error(
        val errorMessage: String
    ) : ChatUiState
}

data class UIConversationItem(
    val author: Author,
    val message: String
)

enum class Author(val color:Color, val displayName:String, val icon: ImageVector) {
    USER(Color.Blue.copy(alpha = .2f), "Your Question", Icons.Outlined.Person),
    GEMINI(Color.Red.copy(alpha = .2f), "Oracle of Delphi", Icons.Filled.Face)
}