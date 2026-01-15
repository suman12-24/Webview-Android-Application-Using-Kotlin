package  wekarthub.inc.sudhritifooddeliveryhotel.webview

import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import  wekarthub.inc.sudhritifooddeliveryhotel.util.Constants.TAG
import androidx.annotation.RequiresApi
import  wekarthub.inc.sudhritifooddeliveryhotel.util.Constants


class   qWebViewClient(
    private val events: WebViewEvents
) : WebViewClient() {

    override fun onReceivedError(
        webView: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        events.onError(description ?: Constants.GENERIC_ERROR_MESSAGE)
        Log.d(TAG, "onReceivedError: $description")
        super.onReceivedError(webView, errorCode, description, failingUrl)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url.toString()
        val isRedirectUrl = RedirectUrls.isRedirectUrl(url)
        if (isRedirectUrl) {
            events.onRedirectUrl(Uri.parse(url))
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        events.onError(error?.description?.toString() ?: Constants.GENERIC_ERROR_MESSAGE)
        Log.d(TAG, "onReceivedError: ${error?.description}")
        super.onReceivedError(view, request, error)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        events.onPageLoaded()
    }
}