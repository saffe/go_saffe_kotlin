package ai.saffe.go_saffe_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import ai.saffe.go_saffe_kotlin.GoSaffeCaptureView
import android.util.Log

class CaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        val captureView = findViewById<GoSaffeCaptureView>(R.id.goSaffeCaptureView)
        captureView.startCapture(
            captureKey = "7ce7e152-ff28-11ed-97b8-0242ac120003",
            user = "pedro@saffe.ai",
            type = "verification",
            endToEndId = "endToEndId",
            onClose = {
                Log.d("CaptureActivity", "onClose executed")
            },
            onFinish = {
                Log.d("CaptureActivity", "onFinish executed")
            },
            onTimeout = {
                Log.d("CaptureActivity", "onTimeout executed")
            },
            onError = { error ->
                Log.d("CaptureActivity", "onError executed ${error.toString()}")
            },
            onLoad = {
                Log.d("CaptureActivity", "onLoad")
            }
        )
    }
}
