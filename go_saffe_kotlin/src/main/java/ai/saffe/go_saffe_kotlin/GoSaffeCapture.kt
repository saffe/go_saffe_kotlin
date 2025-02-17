package ai.saffe.go_saffe_kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.scottyab.rootbeer.RootBeer
import org.json.JSONObject

class GoSaffeCapture(
    private val context: Context,
    private val captureKey: String?,
    private val user: String?,
    private val type: String?,
    private val endToEndId: String?,
    private val onClose: (() -> Unit)? = null,
    private val onFinish: (() -> Unit)? = null,
    private val onTimeout: (() -> Unit)? = null,
    private val onError: ((String) -> Unit)? = null,
    private val onLoad: (() -> Unit)? = null
) {
    companion object {
        private const val BASE_URL = "https://go.saffe.ai/v0/capture"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2001
    }

    private fun isEmulator(): Boolean {
        return (Build.MANUFACTURER.contains("Genymotion")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.lowercase().contains("droid4x")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.HARDWARE == "goldfish"
                || Build.HARDWARE == "vbox86"
                || Build.HARDWARE == "ranchu"
                || Build.HARDWARE.lowercase().contains("nox")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.contains("emu")
                || Build.PRODUCT == "sdk"
                || Build.PRODUCT == "google_sdk"
                || Build.PRODUCT == "sdk_x86"
                || Build.PRODUCT == "vbox86p"
                || Build.PRODUCT.lowercase().contains("nox")
                || Build.BOARD.lowercase().contains("nox")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")))
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    fun render(container: ViewGroup) {
        val layout = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(100, 100, Gravity.CENTER)
            visibility = View.VISIBLE
        }

        val webView = WebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowContentAccess = true
                allowFileAccess = true
                mediaPlaybackRequiresUserGesture = false
                javaScriptCanOpenWindowsAutomatically = true
                setGeolocationEnabled(true)
            }
            clearCache(true)
            clearHistory()

            addJavascriptInterface(object {
                @JavascriptInterface
                fun postMessage(message: String) {
                    try {
                        val json = JSONObject(message)

                        if (json.optString("source") == "go-saffe-capture") {
                            val payload = json.optJSONObject("payload")
                            val event = payload?.optString("event") ?: ""

                            when (event) {
                                "close" -> onClose?.invoke()
                                "finish" -> onFinish?.invoke()
                                "timeout" -> onTimeout?.invoke()
                            }
                        }
                    } catch (e: Exception) {
                        onError?.invoke(e.toString())
                    }
                }
            }, "SaffeCapture")

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.let {
                        val resources = request.resources
                        if (resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                            if (ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                request.grant(request.resources)
                            } else {
                                ActivityCompat.requestPermissions(
                                    context as android.app.Activity,
                                    arrayOf(Manifest.permission.CAMERA),
                                    CAMERA_PERMISSION_REQUEST_CODE
                                )
                            }
                        } else {
                            request.grant(request.resources)
                        }
                    }
                }

                override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        callback.invoke(origin, true, false)
                    } else {
                        ActivityCompat.requestPermissions(
                            context as android.app.Activity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_PERMISSION_REQUEST_CODE
                        )
                        pendingGeoCallback = callback
                        pendingGeoOrigin = origin
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.evaluateJavascript(
                        """
                            (function() {
                                window.addEventListener("message", function(event) {
                                    if (window.SaffeCapture) {
                                        window.SaffeCapture.postMessage(JSON.stringify(event.data));
                                    }
                                }, false);
                                
                                console.log("Listener adicionado com sucesso.");
                            })();
                        """
                    ) { }

                    progressBar.visibility = View.GONE
                    onLoad?.invoke()
                }
            }
        }

        WebView.setWebContentsDebuggingEnabled(true)

        val jsonBody = JSONObject().apply {
            put("capture_key", captureKey ?: JSONObject.NULL)
            put("user_identifier", user ?: JSONObject.NULL)
            put("type", type ?: JSONObject.NULL)
            put("end_to_end_id", endToEndId ?: JSONObject.NULL)
            put("device_context", getDeviceContext())
        }
        val postData = jsonBody.toString().toByteArray(Charsets.UTF_8)

        webView.postUrl(BASE_URL, postData)

        layout.addView(webView)
        layout.addView(progressBar)

        container.addView(layout)
    }

    private fun getDeviceContext(): String {
        val rootBeer = RootBeer(context)
        val contextJson = JSONObject().apply {
            put("isJailBroken", rootBeer.isRooted)
            put("isRealDevice", !isEmulator())
            put("isOnExternalStorage", isExternalStorageAvailable())
        }

        return contextJson.toString()
    }

    private fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private var pendingGeoCallback: GeolocationPermissions.Callback? = null
    private var pendingGeoOrigin: String? = null

    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("GoSaffeCapture", "Permissão de localização concedida pelo usuário.")
                pendingGeoCallback?.invoke(pendingGeoOrigin, true, false)
            } else {
                Log.d("GoSaffeCapture", "Permissão de localização negada pelo usuário.")
                pendingGeoCallback?.invoke(pendingGeoOrigin, false, false)
            }
            pendingGeoCallback = null
            pendingGeoOrigin = null
        }
    }
}
