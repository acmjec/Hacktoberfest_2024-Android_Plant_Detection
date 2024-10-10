package app.acmjec.plantdetector

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageSearchViewModel : ViewModel() {

    private val _uriState = MutableStateFlow("")
    val uriState = _uriState.asStateFlow()

    private val _explanation = MutableStateFlow<String?>(null)
    val explanation = _explanation.asStateFlow()

    // Simulate sending image and getting explanation
    fun sendImageAndGetExplanation(bitmap: Bitmap) {
        viewModelScope.launch {
            // Simulate API call to Gemini and return an explanation (replace with real API call)
            val simulatedExplanation = "This is a placeholder explanation for the selected image."
            _explanation.update { simulatedExplanation }
        }
    }
}
