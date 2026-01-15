package  wekarthub.inc.sudhritifooddeliveryhotel.webview

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class WebView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): WebView(

    context!!, attrs, defStyleAttr
) {

    constructor(context: Context?) : this(context, null) {}
    constructor(context: Context?, attrs: AttributeSet?) : this(

        context,
        attrs,
        android.R.attr.webViewStyle
    )

    private var events: WebViewEvents = WebViewEvents()

    fun initialize(webViewEvents: WebViewEvents) {
        events = webViewEvents
        val webClient = WebViewClient()

        val chromeClient = ChromeClient(events)
        initSettings()
        webViewClient = webClient
        webChromeClient = chromeClient
    }

    private fun initSettings() {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            context.cacheDir.deleteRecursively()
            javaScriptCanOpenWindowsAutomatically = true
            setBuiltInZoomControls(false)
            setDisplayZoomControls(false)
            setSupportZoom(false)
            databaseEnabled = true

            CookieSyncManager.getInstance().startSync()
            CookieManager.getInstance().setAcceptThirdPartyCookies(this@WebView, true)
        }

        setUpDownloadListener()
    }

    private fun setUpDownloadListener() {
        this.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                val cookies = CookieManager.getInstance().getCookie(url)
                addRequestHeader("cookie", cookies)
                addRequestHeader("User-Agent", userAgent)
                setDescription("Downloading ${guessTitle(url, contentDisposition, mimeType)}")
                setTitle(guessTitle(url, contentDisposition, mimeType))
                allowScanningByMediaScanner()
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(

                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    guessTitle(url, contentDisposition, mimeType)


                )
            }

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Toast.makeText(
                this.context,
                "Downloading ${guessTitle(url, contentDisposition, mimeType)}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun guessTitle(url: String?, contentDisposition: String?, mimeType: String?): String? {
        return URLUtil.guessFileName(url, contentDisposition, mimeType)
    }
}
