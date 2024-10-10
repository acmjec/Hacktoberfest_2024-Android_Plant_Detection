package app.acmjec.plantdetector


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Use a handler to delay the transition to the MainActivity
        Handler().postDelayed({
            // Navigate to the MainActivity after a delay
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the SplashActivity
        }, 2000) // Delay for 3 seconds
    }
}
