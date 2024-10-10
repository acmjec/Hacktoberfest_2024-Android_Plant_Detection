package app.acmjec.plantdetector.data

import android.graphics.Bitmap

data class Chat(
    val prompt: String,
    val bitmap: Bitmap?,
    val isFromUser: Boolean
)

data class ChatState(
    val chatList: List<Chat> = emptyList(),
    val bitmap: Bitmap? = null
)

sealed class ChatUiEvent {
    data class SendImage(val bitmap: Bitmap?) : ChatUiEvent()
}
