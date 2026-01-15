package  wekarthub.inc.sudhritifooddeliveryhotel

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import  wekarthub.inc.sudhritifooddeliveryhotel.databinding.ActivityMainBinding
import  wekarthub.inc.sudhritifooddeliveryhotel.util.*
import  wekarthub.inc.sudhritifooddeliveryhotel.util.Constants.TAG
import  wekarthub.inc.sudhritifooddeliveryhotel.webview.WebViewEvents

class MainActivity : AppCompatActivity() {

    private var uploadCallback: ValueCallback<Array<Uri?>?>? = null
    private val filesPicker: FilesPicker by FilesPickerImpl(
        activity = this,
        onCancel = {
            uploadCallback?.onReceiveValue(null)
            uploadCallback = null
            dismissProgressDialog()
        },
        targetFileSize = Constants.targetFileSize,
        maxFileSizeAfterCompress = Constants.maxFileSizeAfterCompression,
        maxFileSizeBeforeCompress = Constants.maxFileSizeBeforeCompression
    )

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val webView:  wekarthub.inc.sudhritifooddeliveryhotel.webview.WebView
        get() = binding.webView

    private lateinit var progressDialog: Dialog


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this), null, false)
        setContentView(binding.root)

        initView()

        onNotificationPermissionHandled()

        val appLinkIntent: Intent = intent
        val appLinkAction: String? = appLinkIntent.action
        val appLinkData: Uri? = appLinkIntent.data
    }

    private fun onNotificationPermissionHandled() {

            initWebView()
            binding.webView.loadUrl("file:///android_asset/a.html")

            webView.setWebViewClient(object : WebViewClient() {
                override fun shouldOverrideUrlLoading(wv: WebView?, url: String): Boolean {
                    if (url.startsWith("tel:") || url.startsWith("mailto:"))   {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                        return true
                    }
                    return false
                }
            })
        }


    private fun initView() {
        initProgressDialog()
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getSupportActionBar()?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorname)


        with(binding) {
            testButton.setOnClickListener {
                filesPicker.pickFiles {
                    it.toString().showToast(this@MainActivity)
                    Log.d(TAG, "picked Uris -> $it")
                }
            }


        }
    }
    private fun initWebView() {
        val events = WebViewEvents(onError = { message ->
            Log.d("1221", "initWebView: error --> $message")
            try {
                webView.stopLoading()
            } catch (_: Exception) {
            }
            if (webView.canGoBack()) webView.goBack()

            webView.loadUrl("about:blank")
            showErrorDialog {
                finish()
                startActivity(intent)
            }
        }, onPageLoadProgress = { newProgress ->
            val isLoaded = newProgress == 100
            if (isLoaded) this@MainActivity.lifecycleScope.launchWhenStarted {

            }
        }, onPageLoaded = {
            this@MainActivity.lifecycleScope.launchWhenStarted {

            }
        }, onRedirectUrl = { uri ->
            openUrl(uri)
        }, onFilesPick = {
            uploadCallback = it
            Log.d("debug", "$uploadCallback")
            launchProgressDialog()
            filesPicker.pickFiles {
                uploadCallback?.onReceiveValue(it?.toTypedArray())
                uploadCallback = null
                dismissProgressDialog()
            }
        },
        )

        webView.initialize(events)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private fun initProgressDialog(): Dialog {
        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.progress_dialog_view)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(0))
        return progressDialog
    }

    private fun launchProgressDialog() {
        if (this.isFinishing) return
        if (::progressDialog.isInitialized && !progressDialog.isShowing) progressDialog.show()
    }

    private fun dismissProgressDialog() {
        if (this.isFinishing) return
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }


    override fun onDestroy() {

        super.onDestroy()
        _binding = null
    }
}

