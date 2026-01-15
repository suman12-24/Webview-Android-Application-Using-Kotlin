package  wekarthub.inc.sudhritifooddeliveryhotel.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri

fun Context.openUrl(uri: Uri){
    try{
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        startActivity(intent)
    } catch (e: Exception){
        "No app found to perform this action".showShortToast(this)
    }
}

fun Context.showErrorDialog(
    onPositiveClick: () -> Unit
){
    val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
    alertDialog.setTitle("Error")
    alertDialog.setMessage("Check your internet connection and try again.")

    alertDialog.setButton(
        DialogInterface.BUTTON_POSITIVE, "Try Again",

        { dialog, which ->
            onPositiveClick()
        })
    alertDialog.show()
}
