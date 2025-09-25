package com.example.language.ui.makeVoc

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar;
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.databinding.FragmentSelectWayAddVocBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.scale
import java.io.FileOutputStream

class SelectWayAddVocFragment : Fragment() {

    private var _binding: FragmentSelectWayAddVocBinding? = null
    private val binding get() = _binding!!

    // 카메라 & 앨범 런처
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var albumLauncher: ActivityResultLauncher<Intent>

    private val PERMISSION_REQUEST_CODE = 100
    private val MAX_IMAGE_SELECTION = 10
    private var photoFilePath: String? = null
    private var photoUri: Uri? = null

    // progressBar view 초기화 필요
    private val progressBar: ProgressBar? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // 요청 결과 콜백
            permissions.forEach { (permission, isGranted) ->
                if (isGranted) {
                    Log.d("Permission", "$permission granted")
                } else {
                    Log.d("Permission", "$permission denied")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSelectWayAddVocBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = SelectWayAddVocFragmentArgs.fromBundle(requireArguments())
        val vocName = args.vocName

        binding.addVocToPictureBtn.setOnClickListener {
           // showImagePickDialog()
        }
        binding.addVocManuallyBtn.setOnClickListener {
            findNavController().navigate(R.id.action_selectWayAddVocFragment_to_addVocManuallyFragment)
        }
    }

    // 런처 초기화
    private fun initializeLaunchers() {
        // 카메라 런처
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoFilePath?.let { path ->
                    val imageFile = File(path)
                    if (imageFile.exists()) {
                        val fileName = imageFile.name
                        val fileSize = imageFile.length()

                        //progressBar.visibility = View.VISIBLE

                        // FileSender 클래스 정의 필요
                        /* val fileSender = FileSender(requireContext())
                        fileSender.sendFileForOCR_singleDict(
                            imageFile,
                            fileName,
                            fileSize,
                            object : FileSender.FileSenderListener {
                                override fun onProgressUpdate(progress: Int, progressText: String) {}

                                override fun onComplete(serverResponse: Any?) {
                                    progressBar.visibility = View.GONE
                                    if (serverResponse is String) {
                                        Log.d("tagCheck", serverResponse)
                                        val lines = serverResponse.split("\n")
                                        val output = lines.map { parseWord(it) }
                                        movefinalword(output)
                                    }
                                }

                                override fun onError(errorMessage: String) {
                                    progressBar.visibility = View.GONE
                                }
                            }
                        ) */

                        // txt 파일에서 불러오기 (테스트용)
                        /* val txtout = loadFileFormtxt("wordset.txt")
                        if (txtout.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            return@let
                        }
                        val output = txtout.map { parseWord(it) } */
                        // movefinalword(output)
                    }
                }
            }
        }
        // 앨범 런처
        albumLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data = result.data
                val myPhotos = mutableListOf<Uri>()

                // 여러 개 선택
                data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        myPhotos.add(clipData.getItemAt(i).uri)
                    }
                } ?: data?.data?.let { uri ->
                    // 한 개 선택
                    myPhotos.add(uri)
                }

                Toast.makeText(requireContext(), "선택된 사진 수: ${myPhotos.size}", Toast.LENGTH_SHORT).show()

                val files = mutableListOf<File>()
                val fileNames = mutableListOf<String>()
                val fileSizes = mutableListOf<Long>()
                prepareFileInfoFromUris(myPhotos, files, fileNames, fileSizes)

                /*progressBar.visibility = View.VISIBLE
                val fileSender = FileSender(requireContext())
                fileSender.sendFileForOCR_multiDict(files, fileNames, fileSizes,
                    object : FileSender.FileSenderListener {
                        override fun onProgressUpdate(progress: Int, progressText: String) {}

                        override fun onComplete(serverResponse: Any?) {
                            progressBar.visibility = View.GONE
                            when (serverResponse) {
                                is String -> {
                                    Log.d("tagCheck", serverResponse)
                                }
                                is List<*> -> {
                                    val tmp = mutableListOf<String>()
                                    for (now in serverResponse) {
                                        val lines = (now as String).split("\n")
                                        tmp.addAll(lines)
                                    }
                                    val output = tmp.map { parseWord(it) }
                                    movefinalword(output)
                                }
                            }
                        }

                        override fun onError(errorMessage: String) {
                            progressBar.visibility = View.GONE
                        }
                    }) */

                // txt 파일에서 불러오기 (테스트용)
                /* val txtout = loadFileFormtxt("wordset.txt")
                if (txtout.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val output = txtout.map { parseWord(it) } */
                // movefinalword(output)
            }
        }
    }

    // 앨범 열기
    private fun getPictureFromAlbum() {
        doRequest()
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = "image/*"
        }
        albumLauncher.launch(intent)
    }

    // 카메라 열기
    private fun getPictureFromCamera() {
        doRequest()
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile = try {
                createImageFile()
            } catch (ex: IOException) {
                Log.e("ocrtag", "Make image fail", ex)
                null
            }

            photoFile?.let {
                val photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    it
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(cameraIntent)
            }
        }
    }

    // 임시 이미지 파일 생성
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            photoFilePath = absolutePath
        }
    }

    // 이미지 크기 조절
    private fun resizeBitmap(original: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = original.width
        val height = original.height
        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        return original.scale(newWidth, newHeight)
    }

    // 이미지 회전
    private fun rotateBitmap(bitmap: Bitmap, photoPath: String): Bitmap {
        return try {
            val exif = ExifInterface(photoPath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (rotation != 0f) {
                val matrix = Matrix().apply { postRotate(rotation) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap
        }
    }

    // Uri → File 변환
    private fun prepareFileInfoFromUris(
        uriList: List<Uri>,
        fileList: MutableList<File>,
        nameList: MutableList<String>,
        sizeList: MutableList<Long>,
    ) {
        val resolver = requireContext().contentResolver
        for (uri in uriList) {
            try {
                var name = "unknown"
                var size: Long = -1
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) name = cursor.getString(nameIndex)
                        if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                    }
                }

                val tempFile = File(requireContext().cacheDir, name)
                resolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                fileList.add(tempFile)
                nameList.add(name)
                sizeList.add(size)
            } catch (e: Exception) {
                Log.e("FilePrep", "파일 준비 실패", e)
            }
        }
    }

    // 권한 요청
    private fun doRequest() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        // 권한이 이미 허용됐는지 체크
        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            // 권한 요청
            requestPermissionLauncher.launch(deniedPermissions.toTypedArray())
        } else {
            // 이미 권한 허용됨
            Log.d("Permission", "All permissions already granted")
        }
    }

}