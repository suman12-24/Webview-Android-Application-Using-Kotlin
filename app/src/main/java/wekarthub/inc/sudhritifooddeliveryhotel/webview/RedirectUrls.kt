package  wekarthub.inc.sudhritifooddeliveryhotel.webview

object RedirectUrls {
     private val urlList = listOf(
        "whatsapp",
        "api.whatsapp.com",
        "Whatsapp.com",
        "api.facebook.com",
        "facebook",
        "facebook.com",
        "mailto",
        "sms",
        "tel",
        "GooglePlayStore",
         "play.google.com"
    )

    fun isRedirectUrl(url: String) = urlList.find { url.contains(it) } != null
}