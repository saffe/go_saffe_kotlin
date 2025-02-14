package ai.saffe.go_saffe_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnIniciar = findViewById<Button>(R.id.btnIniciarCaptura)
        btnIniciar.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java).apply {
                putExtra("captureKey", "")
                putExtra("user", "")
                putExtra("type", "verification")
                putExtra("endToEndId", "endToEndID")
            }
            startActivity(intent)
        }
    }
}
