# go-saffe-capture

Lib para renderizar o Saffe Capture (captura via WebView com integração de parâmetros e indicador de loading) em projetos Android.

## Instalação

### Como módulo local (Android Studio)

1. No seu projeto, adicione o módulo da lib:
    - Vá em **File > New > Import Module** e selecione a pasta onde está a lib.
2. No arquivo `settings.gradle`, inclua:
   ```kotlin
   include(":go-saffe-capture")
   ```
3. No `build.gradle` do módulo do seu app, adicione a dependência:
   ```kotlin
   dependencies {
       implementation(project(":go-saffe-capture"))
   }
   ```

> **Observação:** Se a lib for publicada em um repositório (por exemplo, JitPack ou Maven Central), você poderá adicioná-la como dependência normal.

## Uso

### Exemplo com Jetpack Compose

A seguir, um exemplo que mostra uma tela inicial com um botão "Iniciar Captura". Ao clicar, a tela muda para outra que renderiza a WebView usando a sua lib, passando os parâmetros necessários:

```kotlin
import ai.saffe.go_saffe_kotlin.GoSaffeCapture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.FrameLayout

@Composable
fun MainScreen() {
    var showCapture by remember { mutableStateOf(false) }
    
    if (!showCapture) {
        // Tela inicial com o botão "Iniciar Captura"
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { showCapture = true }) {
                Text(text = "Iniciar Captura")
            }
        }
    } else {
        // Tela de captura com a WebView renderizada pela lib
        WebViewCaptureScreen(
            captureKey = "SUA_CAPTURE_KEY",
            user = "usuario@exemplo.com",
            type = "verification", // ou "onboarding"
            endToEndId = "endToEndId",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun WebViewCaptureScreen(
    captureKey: String,
    user: String,
    type: String,
    endToEndId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Cria um container para a WebView
            val container = FrameLayout(ctx)
            // Instancia a classe customizada que renderiza a WebView com loading
            val capture = GoSaffeCapture(
                context = ctx,
                captureKey = captureKey,
                user = user,
                type = type,
                endToEndId = endToEndId
            )
            capture.render(container)
            container
        }
    )
}
```

### Parâmetros

- **captureKey:** Chave de captura (sandbox ou produção).
- **user:** Identificador do usuário (e-mail ou CPF).
- **type:** Tipo de captura, podendo ser "onboarding" ou "verification".
- **endToEndId:** Identificador para manter a consistência entre front e backend.

> **Nota:** Nesta versão a lógica de callbacks (como onLoad, onError, onClose, onFinish e onTimeout) pode ser implementada internamente na lib, sem que o usuário precise configurar manualmente.

## Contribuindo

Veja o [contributing guide](CONTRIBUTING.md) para aprender como contribuir para o repositório e o fluxo de desenvolvimento.

## License

MIT