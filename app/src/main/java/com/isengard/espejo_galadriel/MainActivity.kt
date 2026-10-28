package com.isengard.espejo_galadriel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity: AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    // URL del streaming (cámbiala por la que pida tu profesor)
    private val streamUrl = "https://www.youtube.com/watch?v=ZfTU67ZIK3Q&list=PLSR2vNOypvs5yLsbqZc0e6RdqNnP1eGIc"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webViewPalantir)
        progressBar = findViewById(R.id.progressBar)

        webView.settings.apply {
            javaScriptEnabled = true               // necesario para el streaming y la inyección
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true            // responsivo: ajusta al ancho
            useWideViewPort = true
            setSupportZoom(false)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                // Inyección de JavaScript: estilo "espejo de agua" sobre la página cargada
                view?.evaluateJavascript(
                    """
                    (function() {
                        document.body.style.margin = '0';
                        document.body.style.backgroundColor = '#0b1d2a';
                        document.body.style.borderRadius = '24px';
                        document.body.style.boxShadow = 'inset 0 0 40px #7fd4ff';
                        var v = document.querySelector('video');
                        if (v) { v.style.width = '100%'; v.style.height = 'auto'; }
                    })();
                    """.trimIndent(), null
                )
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }

        // Cargamos el streaming dentro de un iframe responsivo (16:9) inyectando HTML
        val html = """
            <html><head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
              body { margin:0; background:#0b1d2a; }
              .espejo { position:relative; width:100%; padding-top:56.25%; }
              .espejo iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:0; }
            </style></head>
            <body>
              <div class="espejo">
                <iframe src="$streamUrl" allow="autoplay; encrypted-media" allowfullscreen></iframe>
              </div>
            </body></html>
        """.trimIndent()
        webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)

        // Atrás navega dentro del WebView antes de cerrar la app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}