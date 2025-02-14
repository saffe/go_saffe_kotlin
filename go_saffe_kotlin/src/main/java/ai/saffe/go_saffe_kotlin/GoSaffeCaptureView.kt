package ai.saffe.go_saffe_kotlin

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class GoSaffeCaptureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    fun startCapture(
        captureKey: String,
        user: String,
        type: String,
        endToEndId: String,
        onClose: (() -> Unit)? = null,
        onFinish: (() -> Unit)? = null,
        onTimeout: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
        onLoad: (() -> Unit)? = null
    ) {
        removeAllViews()
        val capture = GoSaffeCapture(
            context = context,
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
        capture.render(this)
    }
}
