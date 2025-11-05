package com.example.language.ui.makeVoc

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.language.api.ApiClient.uploadImagesForDictionary
import com.example.language.api.ApiResponse
import com.example.language.databinding.FragmentSelectWayAddVocBinding
import com.example.language.ui.dialog.PictureSelectDialogFragment
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class SelectWayAddVocFragment : Fragment(), PictureSelectDialogFragment.OnPictureSelectListener {

    private var _binding: FragmentSelectWayAddVocBinding? = null
    private val binding get() = _binding!!

    val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // URI에서 파일 정보와 바이트 배열 가져오기
            val fileName = getFileNameFromUri(requireContext(), uri)
            val fileBytes = getBytesFromUri(requireContext(), uri)

            if (fileBytes != null) {
                val fileSize = fileBytes.size.toLong()

                // 8. API 업로드 함수 호출
                callUploadApi(
                    fileNames = listOf(fileName),
                    fileSizes = listOf(fileSize),
                    fileBytes = fileBytes
                )
            } else {
                showToast("이미지를 불러오는 데 실패했습니다.")
            }
        }
    }

    val cameraThumbnailLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Bitmap에서 바이트 배열 가져오기
            val fileBytes = getBytesFromBitmap(bitmap)
            val fileSize = fileBytes.size.toLong()
            val fileName = "camera_${System.currentTimeMillis()}.jpg" // 임시 파일명 생성

            callUploadApi(
                fileNames = listOf(fileName),
                fileSizes = listOf(fileSize),
                fileBytes = fileBytes
            )
        }
    }

    val galleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            launchGallery()
        } else {
            showToast("갤러리 권한이 거부되었습니다.")
        }
    }

    val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            showToast("카메라 권한이 거부되었습니다.")
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

        binding.addToPictureBtn.setOnClickListener {
            showPictureDialog()
        }

        binding.addToManualBtn.setOnClickListener {
            showToast("직접 입력 버튼 클릭됨")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showPictureDialog() {
        val dialog = PictureSelectDialogFragment()
        dialog.setListener(this)

        // "tag"는 다이얼로그를 식별하는 이름표입니다.
        dialog.show(parentFragmentManager, "PictureSelectDialog")
    }

    private fun checkGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            launchGallery()
        } else {
            galleryPermissionLauncher.launch(permission)
        }
    }

    private fun checkCameraPermission() {
        val permission = Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(permission)
        }
    }

    private fun launchGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun launchCamera() {
        cameraThumbnailLauncher.launch(null)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onGallerySelected() {
        // 다이얼로그에서 "앨범"이 선택되면 여기가 호출됨
        checkGalleryPermission()
    }

    override fun onCameraSelected() {
        // 다이얼로그에서 "카메라"가 선택되면 여기가 호출됨
        checkCameraPermission()
    }

    private fun callUploadApi(fileNames: List<String>, fileSizes: List<Long>, fileBytes: ByteArray) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showToast("업로드 시작...")

                val response = uploadImagesForDictionary(
                    context = requireContext(),
                    fileNames = fileNames,
                    fileSizes = fileSizes,
                    combinedFileBytes = fileBytes
                )

                when (response) {
                    is ApiResponse.Success -> {
                        val words = response.data.data // List<WordData>
                        showToast("업로드 성공! 단어 ${words.size}개 인식됨")
                        val wordsArray = words.toTypedArray()

                        val action = SelectWayAddVocFragmentDirections
                            .actionSelectWayAddVocFragmentToAddVocFinalCheckFragment(wordsArray)

                        findNavController().navigate(action)
                    }
                    is ApiResponse.Error -> {
                        // API 에러
                        showToast("업로드 실패: ${response.message} (코드: ${response.code})")
                    }
                }
            } catch (e: Exception) {
                showToast("업로드 중 오류 발생: ${e.message}")
                Log.e("SelectWayAddVocFragment", "Upload failed", e)
            }
        }
    }

    private fun getBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            Log.e("MyFragment", "Failed to get bytes from Uri", e)
            null
        }
    }

    private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        return outputStream.toByteArray()
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName = "unknown_file_${System.currentTimeMillis()}"
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("MyFragment", "Failed to get file name from Uri", e)
        }
        return fileName
    }
}