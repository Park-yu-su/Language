package com.example.language.ui.makeVoc

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.api.ApiResponse
import com.example.language.api.login.UserPreference
import com.example.language.data.repository.WordbookRepository
import com.example.language.data.WordData as AppWordData
import com.example.language.databinding.DialogEditWordBinding
import com.example.language.databinding.FragmentSelectWayAddVocBinding
import com.example.language.ui.dialog.PictureSelectDialogFragment
import com.example.language.viewModel.VocViewModel
import com.example.language.viewModel.VocViewModelFactory
import java.io.ByteArrayOutputStream

class SelectWayAddVocFragment : Fragment(), PictureSelectDialogFragment.OnPictureSelectListener {

    private var _binding: FragmentSelectWayAddVocBinding? = null
    private val binding get() = _binding!!

    // --- [ ✨ 1. Factory, Repository, Preference 선언 ✨ ] ---
    // (ViewModel 선언보다 *먼저* 선언해야 합니다)
    private val userPreference by lazy {
        UserPreference(requireContext().applicationContext)
    }
    private val repository by lazy {
        WordbookRepository(userPreference)
    }
    private val viewModelFactory by lazy {
        VocViewModelFactory(repository)
    }

    // --- [ ✨ 2. ViewModel 선언 (수정) ✨ ] ---
    // 팩토리를 람다로 전달합니다.
    private val viewModel: VocViewModel by activityViewModels<VocViewModel> { viewModelFactory }

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
            showManualDialog()
        }

        observeViewModel()
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

    private fun showManualDialog() {
        val dialogBinding =
            DialogEditWordBinding.inflate(LayoutInflater.from(requireContext()))
        val dialogView = dialogBinding.root

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("단어 추가하기")
            .setView(dialogView)
            .setPositiveButton("추가") { dialog, _ ->
                val newWord = dialogBinding.inputWord.text.toString().trim()
                val newExample = dialogBinding.inputExample.text.toString().trim()

                val newMeanings = mutableListOf<String>()
                listOf(
                    dialogBinding.inputMeaning1,
                    dialogBinding.inputMeaning2,
                    dialogBinding.inputMeaning3,
                    dialogBinding.inputMeaning4
                ).forEach { editText ->
                    val meaning = editText.text.toString().trim()
                    if (meaning.isNotEmpty()) {
                        newMeanings.add(meaning)
                    }
                }

                if (newWord.isNotEmpty() && newMeanings.isNotEmpty()) {

                    val newAppWordData = AppWordData(
                        word = newWord,
                        example = newExample,
                        meanings = newMeanings
                    )

                    // 1. ViewModel에 수동 추가된 단어 저장
                    viewModel.addManualWord(newAppWordData)

                    // 2. Safe Args 없이 다음 화면으로 이동 (R.id.action...은 NavGraph에 따름)
                    findNavController().navigate(R.id.action_selectWayAddVocFragment_to_addVocFinalCheckFragment)

                    dialog.dismiss()

                } else {
                    Toast.makeText(
                        requireContext(),
                        "단어와 최소한 하나의 뜻은 필수입니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // 둥근 모서리를 표현하기 위해
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        alertDialog.show()

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
        showToast("업로드 시작...")

        // ViewModel의 함수를 호출
        // (Context는 ApplicationContext를 넘겨 메모리 누수 방지)
        viewModel.uploadDictionaryImages(
            context = requireContext().applicationContext,
            fileNames = fileNames,
            fileSizes = fileSizes,
            combinedFileBytes = fileBytes
        )
    }

    // API 호출의 "결과" (isLoading, analysisStatus)는 여기서 처리
    private fun observeViewModel() {

        // API 분석 결과 상태 관찰
        viewModel.analysisStatus.observe(viewLifecycleOwner) { response ->
            // null이 아니면 (새로운 이벤트가 도착하면) 처리
            if (response != null) {
                when (response) {
                    is ApiResponse.Success -> {
                        val words = response.data.data // List<WordData> (API 모델)
                        showToast("업로드 성공! 단어 ${words.size}개 인식됨")

                        // ViewModel의 wordList는 이미 갱신되었음
                        // Safe Args 없이 다음 화면으로 이동
                        findNavController().navigate(R.id.action_selectWayAddVocFragment_to_addVocFinalCheckFragment)
                    }
                    is ApiResponse.Error -> {
                        // API 에러
                        showToast("업로드 실패: ${response.message} (코드: ${response.code})")
                    }
                }

                // [ 중요 ] 처리 완료 후, ViewModel의 상태를 null로 리셋
                // (화면 회전 시 이벤트가 다시 실행되는 것을 방지)
                viewModel.resetAnalysisStatus()
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