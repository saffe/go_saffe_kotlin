package ai.saffe.go_saffe_kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ai.saffe.go_saffe_kotlin.ui.theme.GoSaffeKotlinTheme
import android.util.Log

class MainActivity : ComponentActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val LOCATION_PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        setContent {
            GoSaffeKotlinTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var showWebView by remember { mutableStateOf(false) }

    if (!showWebView) {
        InitialScreen(onStartCapture = { showWebView = true })
    } else {
        WebViewLibraryScreen(
            captureKey = "",
            user = "",
            type = "verification",
            endToEndId = "endToEndID",
            onClose = { Log.i("Main", "close") },
            onFinish = { Log.i("Main", "finish") },
            onTimeout = { Log.i("Main", "timeout") },
            onError = { Log.i("Main", "error") },
            onLoad = { Log.i("Main", "load") },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun InitialScreen(onStartCapture: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onStartCapture) {
            Text(text = "Iniciar Captura")
        }
    }
}

@Composable
fun WebViewLibraryScreen(
    captureKey: String?,
    user: String?,
    type: String?,
    endToEndId: String?,
    onClose: (() -> Unit)? = null,
    onFinish: (() -> Unit)? = null,
    onTimeout: (() -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    onLoad: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val container = FrameLayout(ctx)
            val capture = GoSaffeCapture(
                context = ctx,
                captureKey = captureKey,
                user = user,
                type = type,
                endToEndId = endToEndId,
                onClose = onClose,
                onFinish = onFinish,
                onTimeout = onTimeout,
                onError = onError,
                onLoad = onLoad
            )
            capture.render(container)
            container
        },
        update = {}
    )
}
