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
import android.os.Environment
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
import com.example.language.viewModel.AppWordData
import com.example.language.databinding.DialogEditWordBinding
import com.example.language.databinding.FragmentSelectWayAddVocBinding
import com.example.language.ui.dialog.PictureSelectDialogFragment
import com.example.language.viewModel.VocViewModel
import com.example.language.viewModel.VocViewModelFactory
import android.view.WindowManager
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectWayAddVocFragment : Fragment(), PictureSelectDialogFragment.OnPictureSelectListener {

    private var _binding: FragmentSelectWayAddVocBinding? = null
    private val binding get() = _binding!!

    private var tempImageUri: Uri? = null

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

    val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            // 사진 촬영에 성공하면, 멤버 변수에 저장해둔 tempImageUri를 사용합니다.
            tempImageUri?.let { uri ->
                try {
                    // Uri에서 InputStream을 열어 바이트 배열(ByteArray)을 읽어옵니다.
                    val fileBytes = requireContext().contentResolver.openInputStream(uri)?.readBytes()
                    val fileSize = fileBytes?.size?.toLong() ?: 0L
                    val fileName = "camera_${System.currentTimeMillis()}.jpg"

                    if (fileBytes != null && fileSize > 0) {
                        // 원본 코드의 업로드 API 호출
                        callUploadApi(
                            fileNames = listOf(fileName),
                            fileSizes = listOf(fileSize),
                            fileBytes = fileBytes
                        )
                    }

                    // 사용이 끝난 임시 Uri 변수 정리 (선택적)
                    tempImageUri = null

                } catch (e: Exception) {
                    Log.e("CameraUpload", "Failed to read image bytes from URI", e)
                }
            }
        } else {
            // 사용자가 카메라를 취소했거나 오류가 발생한 경우
            Log.d("CameraUpload", "Camera capture cancelled or failed")
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

        binding.selectWayAddVocBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unblockScreenTouch()
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
                // (새로운 단어/뜻 파싱...)
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

                    // [ ✨ 핵심 ✨ ]
                    // 1. UI용 AppWordData 생성 (새 단어이므로 wordId = 0)
                    val newAppWordData = AppWordData(
                        wordId = 0, // [!] 새 단어
                        word = newWord,
                        example = newExample,
                        meanings = newMeanings,
                        distractors = emptyList() // (기본값)
                    )

                    // 2. ViewModel에 단어 추가
                    viewModel.addManualWord(newAppWordData)

                    // 3. Safe Args 없이 다음 화면으로 이동
                    findNavController().navigate(R.id.action_selectWayAddVocFragment_to_addVocFinalCheckFragment)

                    dialog.dismiss()

                } else {
                    Toast.makeText(requireContext(), "단어와 최소한 하나의 뜻은 필수입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

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
        try {
            // 1. 사진을 저장할 임시 파일을 생성합니다.
            val photoFile: File = createImageFile()

            // 2. FileProvider를 사용하여 파일에 대한 content:// URI를 생성합니다.
            //    (AndroidManifest.xml 및 filepaths.xml에 FileProvider 설정이 되어있어야 합니다.)
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )

            // 3. 생성된 URI를 나중에 콜백에서 사용할 수 있도록 멤버 변수에 저장합니다.
            tempImageUri = photoURI

            // 4. Launcher를 실행하며 저장할 위치(URI)를 전달합니다.
            cameraLauncher.launch(photoURI)

        } catch (ex: IOException) {
            Log.e("CameraUpload", "Failed to create image file", ex)
            showToast("이미지 파일 생성에 실패했습니다.")
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 타임스탬프를 사용하여 고유한 파일 이름 생성
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* 접두사 */
            ".jpg",               /* 접미사 */
            storageDir            /* 디렉토리 */
        )
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

    /**
     * [ ✨ 6. 사진 분석 API 호출✨ ]
     * (ViewModel 호출)
     */
    private fun callUploadApi(fileNames: List<String>, fileSizes: List<Long>, fileBytes: ByteArray) {
        showToast("업로드 시작...")
        // ViewModel의 함수를 호출 (Context는 ApplicationContext 전달)
        viewModel.uploadDictionaryImages(
            context = requireContext().applicationContext,
            fileNames = fileNames,
            fileSizes = fileSizes,
            combinedFileBytes = fileBytes
        )
    }

    /**
     * [ ✨ 7. ViewModel 관찰 ✨ ]
     * (사진 분석 API 결과 처리)
     */
    private fun observeViewModel() {
        // [ ✨ 로딩 상태 관찰 (화면 터치 제어) ✨ ]
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // 로딩 중일 때 화면 터치 막기
                blockScreenTouch()
            } else {
                // 로딩이 끝나면 화면 터치 풀기
                unblockScreenTouch()
            }
        }

        // API 분석 결과 상태 관찰
        viewModel.analysisStatus.observe(viewLifecycleOwner) { response ->
            response ?: return@observe // 이벤트가 없으면 리턴

            when (response) {
                is ApiResponse.Success -> {
                    val words = response.data.data
                    showToast("업로드 성공! 단어 ${words.size}개 인식됨")

                    // [ ✨ 핵심 ✨ ]
                    // ViewModel의 wordList는 이미 갱신되었음
                    // Safe Args 없이 다음 화면으로 이동
                    findNavController().navigate(R.id.action_selectWayAddVocFragment_to_addVocFinalCheckFragment)
                }
                is ApiResponse.Error -> {
                    showToast("업로드 실패: ${response.message} (코드: ${response.code})")
                }
            }
            // [!] 이벤트 소비 후 리셋
            viewModel.resetAnalysisStatus()
        }
    }

    // --- [ ✨ 화면 터치 제어 헬퍼 함수 ✨ ] ---

    /**
     * Activity의 Window에 Flag를 설정하여 화면 전체의 터치를 막습니다.
     */
    private fun blockScreenTouch() {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    /**
     * 설정했던 Flag를 제거하여 화면 터치를 다시 활성화합니다.
     */
    private fun unblockScreenTouch() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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