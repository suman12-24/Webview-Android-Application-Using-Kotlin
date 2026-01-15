package  wekarthub.inc.sudhritifooddeliveryhotel.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.MimeTypeMap.getFileExtensionFromUrl
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

suspend fun Context.uriToImageFile(uri: Uri): File? {
    val file = createImageFile()
    with(Dispatchers.IO) {
        FileOutputStream(file).use { outputStream ->
            this@uriToImageFile.contentResolver.openInputStream(uri).use {
                it?.copyTo(outputStream)
            }
        }
    }

    return file
}

fun Context.createImageFile(): File? {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir: File? = this.cacheDir

    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}

fun Context.createVideoFile(): File? {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir: File? = this.cacheDir

    return File.createTempFile(
        "MP4_${timeStamp}_", /* prefix */
        ".mp4", /* suffix */
        storageDir /* directory */
    )
}

fun isJPEGorPNG(url: String?): Boolean {
    try {
        val type: String = getMimeType(url!!)!!
        val ext = type.substring(type.lastIndexOf("/") + 1)
        if (ext.equals("jpeg", ignoreCase = true) || ext.equals(
                "jpg",
                ignoreCase = true
            ) || ext.equals("png", ignoreCase = true)
        ) {
            return true
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return true
    }
    return false
}

fun getMimeType(url: String): String? {
    var type: String? = null
    val extension = getFileExtensionFromUrl(url)
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}

fun File.isValidSize(maxSizeInKB: Float): Boolean {
    val fileSizeInKB = calcuclateFileSize()
    return fileSizeInKB.toFloat() <= maxSizeInKB

}

//Returns size in KB
fun File.calcuclateFileSize()= this.length()/1024L

suspend fun File.clearContent(){
    with(Dispatchers.IO) {
        PrintWriter(this@clearContent).use {
            it.close()
        }
    }
}

