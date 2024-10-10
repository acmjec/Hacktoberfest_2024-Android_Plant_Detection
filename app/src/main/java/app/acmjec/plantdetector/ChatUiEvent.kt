import android.graphics.Bitmap

// ChatUiEvent.kt
sealed class ChatUiEvent {
    data class SendPrompt(val prompt: String, val bitmap: Bitmap?) : ChatUiEvent()
    data class UpdatePrompt(val newPrompt: String) : ChatUiEvent()
    data class SendImage(val bitmap: Bitmap?) : ChatUiEvent()  // Add this event
}


