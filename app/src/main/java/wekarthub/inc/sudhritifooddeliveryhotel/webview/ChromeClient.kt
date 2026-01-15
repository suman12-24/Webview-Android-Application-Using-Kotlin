package  wekarthub.inc.sudhritifooddeliveryhotel.webview

import android.net.Uri
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import  wekarthub.inc.sudhritifooddeliveryhotel.util.Constants.TAG

class ChromeClient(
    private val events: WebViewEvents
): WebChromeClient() {

    // For Lollipop 5.0+ Devices
    override fun onShowFileChooser(
        mWebView: WebView?,
        filePathCallback: ValueCallback<Array<Uri?>?>,
        fileChooserParams: FileChooserParams

    ): Boolean {
        events.onFilesPick(filePathCallback)
        return true

    }


    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        events.onPageLoadProgress(newProgress)
        Log.d("1221", "onProgressChanged: $newProgress")

        val isLoaded = newProgress == 100
        if (isLoaded){
            val isNetworkError = view?.title?.equals("web page not available", true)  == true
            if (isNetworkError) {
                Log.d(TAG, "network error chrome client")
                events.onError("No internet connection")
            }
        }
    }

}

