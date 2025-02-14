package ai.saffe.go_saffe_kotlin

import android.annotation.SuppressLint
import android.content.Context
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
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
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
        private const val STATIC_URL = "https://go.saffe.ai/v0/capture"
    }

    private fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val device = Build.DEVICE.lowercase()
        val product = Build.PRODUCT.lowercase()

        return (fingerprint.contains("sdk_gphone") ||
                model.startsWith("sdk_gphone") ||
                model.contains("google_sdk") ||
                device.contains("emu") ||
                product.contains("sdk_gphone"))
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
                cacheMode = WebSettings.LOAD_NO_CACHE
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
                                else -> onError?.invoke("Evento desconhecido: $event")
                            }
                        } else {
                            onError?.invoke("Fonte inválida na mensagem")
                        }
                    } catch (e: Exception) {
                        Log.e("GoSaffeCapture", "Erro ao processar mensagem: $message", e)
                        onError?.invoke(e.toString())
                    }
                }
            }, "SaffeCapture")

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(request.resources)
                }
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String,
                    callback: GeolocationPermissions.Callback
                ) {
                    callback.invoke(origin, true, false)
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                    onLoad?.invoke()
                }
            }
        }

        val jsonBody = JSONObject().apply {
            put("capture_key", captureKey ?: JSONObject.NULL)
            put("user_identifier", user ?: JSONObject.NULL)
            put("type", type ?: JSONObject.NULL)
            put("end_to_end_id", endToEndId ?: JSONObject.NULL)
            put("device_context", getDeviceContext())
        }
        val postData = jsonBody.toString().toByteArray(Charsets.UTF_8)
        Log.d("GoSaffeCapture", "POST data: ${jsonBody.toString()}")

        webView.postUrl(STATIC_URL, postData)

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
        Log.d("GoSaffeCapture", "Device context: $contextJson")
        return contextJson.toString()
    }

    private fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}
