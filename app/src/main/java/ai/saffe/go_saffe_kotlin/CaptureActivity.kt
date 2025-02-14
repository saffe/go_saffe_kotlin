package ai.saffe.go_saffe_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import ai.saffe.go_saffe_kotlin.GoSaffeCaptureView

class CaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        val captureView = findViewById<GoSaffeCaptureView>(R.id.goSaffeCaptureView)
                captureView.startCapture(
                        captureKey = intent.getStringExtra("captureKey") ?: "",
                user = intent.getStringExtra("user") ?: "",
                type = intent.getStringExtra("type") ?: "",
                endToEndId = intent.getStringExtra("endToEndId") ?: "",
                onClose = { finish() },
                onFinish = { /* Callback de finalização */ },
                onTimeout = { /* Callback de timeout */ },
                onError = { error -> /* Callback de erro */ },
                onLoad = { /* Callback de carregamento */ }
        )
    }
}
