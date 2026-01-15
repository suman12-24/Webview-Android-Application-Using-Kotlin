package  wekarthub.inc.sudhritifooddeliveryhotel.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import  wekarthub.inc.sudhritifooddeliveryhotel.ChooseMediaAdapter
import  wekarthub.inc.sudhritifooddeliveryhotel.R
import  wekarthub.inc.sudhritifooddeliveryhotel.databinding.BottomSheetChooseOptionBinding
import  wekarthub.inc.sudhritifooddeliveryhotel.util.Constants.TAG
import java.io.File
import java.io.IOException
import kotlin.reflect.KProperty

interface FilesPicker {
    fun pickFiles(onFinalFilesSubmit: (List<Uri>?) -> Unit)
}

class FilesPickerImpl(
    private val contextFactory: () -> Context,
    activityResultCaller: ActivityResultCaller,
    private val lifeCycleOwnerFactory: () -> LifecycleOwner,
    private val onCancel:() -> Unit,
    private val targetFileSize: Long = 1500,
    private val maxFileSizeAfterCompress: Long = 7000,
    private val maxFileSizeBeforeCompress: Long = 1500,
    private val isCompressToTargetFileSize: Boolean = true
) : FilesPicker {

    private suspend fun onFilesSelectedAndCompressed(listFiles: List<Uri>?){
        withContext(Dispatchers.Main.immediate){
            onFinalFilesSubmit(listFiles)
        }
    }

    private var onFinalFilesSubmit: (List<Uri>?) -> Unit = {}

    private val context: Context
        get() = contextFactory()

    private val lifecycleOwner: LifecycleOwner
        get() = lifeCycleOwnerFactory()

    private var isVideoClicked = false
    private var isAnyOptionSelected = false

    private var uriTest: Uri? = null

    constructor(
        fragment: Fragment,
        targetFileSize: Long,
        maxFileSizeAfterCompress: Long,
        maxFileSizeBeforeCompress: Long,
        onCancel: () -> Unit
    ) : this(
        contextFactory = { fragment.requireContext() },
        activityResultCaller = fragment,
        lifeCycleOwnerFactory = { fragment.viewLifecycleOwner },
        onCancel = onCancel,
        targetFileSize = targetFileSize,
        maxFileSizeAfterCompress = maxFileSizeAfterCompress,
        maxFileSizeBeforeCompress = maxFileSizeBeforeCompress
    )

    constructor(
        activity: ComponentActivity,
        onCancel: () -> Unit,
        targetFileSize: Long,
        maxFileSizeAfterCompress: Long,
        maxFileSizeBeforeCompress: Long,
    ) : this(
        contextFactory = { activity },
        activityResultCaller = activity,
        lifeCycleOwnerFactory = { activity },
        onCancel = onCancel,
        targetFileSize = targetFileSize,
        maxFileSizeAfterCompress = maxFileSizeAfterCompress,
        maxFileSizeBeforeCompress = maxFileSizeBeforeCompress
    )

    private var selectedFiles: List<Uri>? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): FilesPicker {
        return this
    }

    private val multipleImagePicker = activityResultCaller.registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        selectedFiles = it
        onSelected(selectedFiles)
    }

    private val cameraLauncher =
        activityResultCaller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                Log.d(TAG, "selectedFilesss: -> $selectedFiles ")
                Log.d(TAG, "selectedFilesss UriTest -> $uriTest: ")
                onSelected(selectedFiles!!, shouldBeCompressed = !isVideoClicked)
            } else onSelected(null)
        }


    override fun pickFiles(onFinalFilesSubmit: (List<Uri>?) -> Unit) {
        this.onFinalFilesSubmit = onFinalFilesSubmit
        doIfHasPermissions{
            launchChoosePickOptionBottomSheet()
        }
    }

    private fun doIfHasPermissions(
        onGranted: () -> Unit
    ) {
        val isApi33OrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val listPermissions = if (isApi33OrHigher) listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        ) else listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        Dexter.withContext(context)
            .withPermissions(listPermissions).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    report.let {
                        if (it.areAllPermissionsGranted()) {
                            onGranted()
                        } else {
                            if (it.isAnyPermissionPermanentlyDenied) {
                                context.getString(
                                    R.string.need_camera_file_permission
                                ).showToast(context)
                                Log.d("appDebug", "denied ${it.deniedPermissionResponses.map { it.permissionName }}")
                                onSelected(null)
                            }
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).withErrorListener {
                it.name.showToast(context)
                Log.d("appDebug", "error ${it.name}")
                onSelected(null)
            }.check()
    }

    private fun launchChoosePickOptionBottomSheet() {
        isAnyOptionSelected = false

        val binding =
            BottomSheetChooseOptionBinding.inflate(LayoutInflater.from(context), null, false)

        val bottomSheetDialogChooseMedia = BottomSheetDialog(context).apply {
//            setOnDismissListener {
//                onCancel()
//                it.dismiss()
//            }
            setOnCancelListener{
                if (!isAnyOptionSelected) onCancel()
            }
        }
        bottomSheetDialogChooseMedia.setContentView(binding.root)

        binding.recyclerViewChooseMedia.layoutManager = LinearLayoutManager(context)
        val chooseMediaAdapter = ChooseMediaAdapter(
            context.resources.getStringArray(R.array.media_option),
            onChooseMediaClick = {
                isVideoClicked = it == 1
//                Comment the position of the option that should be hidden
                when (it) {
                    0 -> {
                        chooseCameraImage()
                        isAnyOptionSelected = true
                    }
//                    1 -> {
//                        chooseCameraVideo()
//                        isAnyOptionSelected = true
//
//                    }
                    1 -> {
                        multipleImagePicker.launch("image/*")
                        isAnyOptionSelected = true
                    }
                    else -> {
                        bottomSheetDialogChooseMedia.cancel()
                        onCancel()
                    }
                }
                bottomSheetDialogChooseMedia.cancel()
            }
        )
        binding.recyclerViewChooseMedia.adapter = chooseMediaAdapter
        bottomSheetDialogChooseMedia.show()
    }

    private fun chooseCameraImage() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(context.packageManager)?.also {
                // Create the File where the photo should go test. wait bro i will check it
                val photoFile: File? = try {
                    context.createImageFile()
                } catch (ex: IOException) {
                    onSelected(null)
                    null
                }

                uriTest = FileProvider.getUriForFile(
                    context, context.packageName.plus(".provider"),
                    context.createImageFile()!!
                )
                if (photoFile == null) {
                    Log.d(TAG, "chooseCamera: error -> null file")
                    return
                }
                // Continue only if the File was successfully created
                photoFile.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context, context.packageName.plus(".provider"),
                        it
                    )
                    takePictureIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                    takePictureIntent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    Log.d(TAG, "chooseCameraImage: URI -> $photoURI")
                    Log.d(TAG, "chooseCameraImage: URITEST -> $uriTest")
                    selectedFiles = listOf(photoURI)
                    cameraLauncher.launch(takePictureIntent)
                }
            }
        }

    }

    private fun chooseCameraVideo() {

        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            // Ensure that there's a camera activity to handle the intent
            takeVideoIntent.resolveActivity(context.packageManager)?.also {
                // Create the File where the photo should go
                val videoFile: File? = try {
                    context.createVideoFile()
                } catch (ex: IOException) {
                    onSelected(null)
                    null
                }

                if (videoFile == null) {
                    Log.d(TAG, "chooseCamera: error -> null file")
                    return
                }
                // Continue only if the File was successfully created
                videoFile.also {
                    val videoUri: Uri = FileProvider.getUriForFile(
                        context, context.packageName.plus(".provider"),
                        it
                    )
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                    selectedFiles = listOf(videoUri)
                    cameraLauncher.launch(takeVideoIntent)
                }
            }
        }

    }

    private fun onSelected(listFiles: List<Uri>?, shouldBeCompressed: Boolean = true) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            if (listFiles == null) {
                onFilesSelectedAndCompressed(null)
                return@launch
            }
            val listFile = listFiles.toFileList()
            val areAllSizeValid = listFile.areAllSizeValid(targetFileSize)

            Log.d(
                TAG,
                "onSelected: files size before compress -> ${listFile.map { it.calcuclateFileSize() }}"
            )

            if (!shouldBeCompressed){
                onFilesSelectedAndCompressed(listFiles)
                return@launch
            }

            when {
                areAllSizeValid -> onFilesSelectedAndCompressed(listFiles)
                isCompressToTargetFileSize -> {
                    val compressedFiles = listFile.compressAll()
                    Log.d(
                        TAG,
                        "onSelected: files sizes -> ${compressedFiles.map { it.calcuclateFileSize() }}"
                    )
                    val areAllSizeAfterCompressValid =
                        compressedFiles.areAllSizeValid(maxFileSizeAfterCompress)
                    if (!areAllSizeAfterCompressValid) {
                        showWrongSizeSnackBar(maxFileSizeBeforeCompress / 1000)
                        onFilesSelectedAndCompressed(null)

                    } else {
                        val compressedUris = compressedFiles.map { it.toUri() }
                        onFilesSelectedAndCompressed(compressedUris)
                    }
                }
                else -> {
                    showWrongSizeSnackBar(targetFileSize / 1000)
                    onFilesSelectedAndCompressed(null)
                }

            }
        }
    }

    private suspend fun showWrongSizeSnackBar(maxFileSizeBeforeCompressInMB: Long) {
        withContext(Dispatchers.Main) {
            context.getString(R.string.upload_file_size_error_msg, maxFileSizeBeforeCompressInMB)
                .showToast(context)
        }
    }

    private suspend fun List<Uri>.toFileList() = mapNotNull {
        context.uriToImageFile(it)
    }

    private fun List<File>.areAllSizeValid(size: Long) = this.all {
        it.isValidSize(size.toFloat())
    }

    private suspend fun List<File>.compressAll() = this.map {
        Compressor.compress(context, it) {
            this.size(targetFileSize * 1024L, stepSize = 15)
        }
    }

}