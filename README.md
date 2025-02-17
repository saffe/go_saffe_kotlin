# go_saffe-kotlin

Kotlin library for Android that renders Go Saffe capture.

---

## 📦 **Installation**

Add the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.saffe:go_saffe_kotlin:1.0.0")
}
```

---

## 🚀 **How to Use**

### 1. **Initial Setup**

In your `AndroidManifest.xml`, add the required permissions:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

> **Note:** Location permission is optional. To enable location, activate it in the settings and add:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

---

### 2. **Using `GoSaffeCaptureView`**

#### **XML**
```xml
<ai.saffe.go_saffe_kotlin.GoSaffeCaptureView
    android:id="@+id/goSaffeCaptureView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

#### **Kotlin**
```kotlin
val captureView = findViewById<GoSaffeCaptureView>(R.id.goSaffeCaptureView)

captureView.startCapture(
    captureKey = "", // capture key (sandbox or production)
    user = "", // user identifier (either email or CPF)
    type = "", // "onboarding" or "verification"
    endToEndId = "", // identifier to keep consistency between front and backend
    onClose = {}, // callback function called when end-user closes (cancels) the capture
    onFinish = {}, // callback function called when end-user finishes (completes) the capture
    onTimeout = {}, // callback function called when the capture ends for timeout
    onError = {}, // callback function called to catch error from component
    onLoad = {} // callback function called when component is loading
)
```

---

## 📄 **License**

MIT

