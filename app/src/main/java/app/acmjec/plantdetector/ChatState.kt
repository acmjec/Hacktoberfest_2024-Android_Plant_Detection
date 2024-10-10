package app.acmjec.plantdetector

import android.graphics.Bitmap
import app.acmjec.plantdetector.data.Chat

/**
 * @author Owais Raza
 */
data class ChatState (
    val chatList: MutableList<Chat> = mutableListOf(),
    val prompt: String = "",
    val bitmap: Bitmap? = null
)