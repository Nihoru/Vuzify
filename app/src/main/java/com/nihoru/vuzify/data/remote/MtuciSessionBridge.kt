package com.nihoru.vuzify.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class MtuciSession(
    val cookies: String,
    val sessid: String?
)

class MtuciSessionBridge(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun obtainSession(): Result<MtuciSession> = suspendCancellableCoroutine { continuation ->
        mainHandler.post {
            try {
                val webView = WebView(context.applicationContext)
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)

                webView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                }

                var isCompleted = false

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val cookies = cookieManager.getCookie("https://mtuci.ru") ?: ""

                        // Check if StormWall hash cookie is present
                        if (cookies.contains("__hash_") && !isCompleted) {
                            // Extract bitrix_sessid from DOM
                            webView.evaluateJavascript("(window.BX && BX.bitrix_sessid) ? BX.bitrix_sessid() : ''") { sessidValue ->
                                val cleanedSessid = sessidValue?.replace("\"", "")?.trim()
                                if (!isCompleted) {
                                    isCompleted = true
                                    webView.destroy()
                                    continuation.resume(Result.success(MtuciSession(cookies, cleanedSessid)))
                                }
                            }
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                    }
                }

                webView.loadUrl("https://mtuci.ru/time-table/")

                // 15 seconds timeout fallback
                mainHandler.postDelayed({
                    if (!isCompleted) {
                        isCompleted = true
                        val fallbackCookies = cookieManager.getCookie("https://mtuci.ru") ?: ""
                        webView.destroy()
                        continuation.resume(Result.success(MtuciSession(fallbackCookies, null)))
                    }
                }, 15000)

            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }
}
