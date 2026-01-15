package  wekarthub.inc.sudhritifooddeliveryhotel.webview

import android.net.Uri
import android.webkit.ValueCallback

class WebViewEvents(

    val onError: (String) -> Unit = {},
    val onPageLoadProgress : (Int) -> Unit = {},
    val onPageLoaded: () -> Unit = {},
    val onRedirectUrl: (Uri) -> Unit = {},
    val onFilesPick: (ValueCallback<Array<Uri?>?>) -> Unit = {},
)