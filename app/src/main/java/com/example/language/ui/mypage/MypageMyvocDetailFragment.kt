package com.example.language.ui.mypage

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.language.R
import com.example.language.adapter.WordListAdapter
import com.example.language.data.WordData
import com.example.language.databinding.DialogCustomSelectBinding
import com.example.language.databinding.FragmentMypageMyvocDetailBinding
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import com.example.language.api.ApiResponse
import com.example.language.api.login.UserPreference
import com.example.language.api.mypage.MypageRepository
import com.example.language.api.mypage.viewModel.MypageViewModel
import com.example.language.api.mypage.viewModel.MypageViewModelFactory
import kotlin.getValue

class MypageMyvocDetailFragment : Fragment() {

    private var _binding: FragmentMypageMyvocDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WordListAdapter
    private var fragmentWordList: MutableList<WordData> = mutableListOf()

    private var tagList: MutableList<String> = mutableListOf()

    private lateinit var userPreference : UserPreference

    private val myPageRepository = MypageRepository()
    private val myPageViewModel: MypageViewModel by activityViewModels() {
        MypageViewModelFactory(myPageRepository)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMypageMyvocDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())

        setWordUI()
        setupRecyclerView()

        // [관찰] 단어 목록 & 단어장 삭제 결과 관찰
        observeViewModel()
        getWords()

        binding.manageMyVocDetailBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.deleteVocBtn.setOnClickListener {
            val title = myPageViewModel.selectWordbookInfo.title
            showDeleteVocDialog(title)
        }

        binding.setTagBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mypageMyVocDetailFragment_to_mypageTagFragment)
        }

        binding.addWordBtn.setOnClickListener {
            // 1. 전달할 인자("isReturnMode")를 Bundle로 만듭니다.
            val bundle = bundleOf("isReturnMode" to true)

            findNavController().navigate(
                R.id.action_mypageMyvocDetailFragment_to_selectWayAddVocFragment,
                bundle
            )
        }

        binding.deleteWordBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mypageMyvocDetailFragment_to_mypageDeleteWordFragment)
        }

        binding.idCopyBtn.setOnClickListener {
            copyID()
        }

        // 'SelectWay...'에서 보낸 결과 리스닝 (단어 추가 후 복귀 시)
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.mypageMyvocDetailFragment)
        navBackStackEntry.savedStateHandle.getLiveData<Boolean>("dialog_completed")
            .observe(viewLifecycleOwner) { result ->
                if (result == true) {
                    Toast.makeText(requireContext(), "단어 추가 완료", Toast.LENGTH_SHORT).show()
                    // 단어 추가 후 목록 즉시 갱신
                    getWords()
                    navBackStackEntry.savedStateHandle.remove<Boolean>("dialog_completed")
                }
            }
    }

    //단어장 UI작업(태그 및 이름)
    private fun setWordUI(){

        val container = binding.tagLayout
        val inflater = LayoutInflater.from(requireContext())

        container.removeAllViews() // 중복 추가 방지

        //태그 추가하기
        var myword = myPageViewModel.selectWordbookInfo
        tagList.clear()
        for(tag in myword.tags){
            tagList.add(tag)
        }

        for (tagText in tagList) {
            val itemView = inflater.inflate(R.layout.item_tag, container, false)
            val textView = itemView.findViewById<TextView>(R.id.tag_id_tv)

            textView.text = tagText

            container.addView(itemView)
        }

        //이름 및 개수 설정
        binding.vocNameTv.text = myPageViewModel.selectWordbookInfo.title
        binding.vocIdTv.text = myPageViewModel.selectWordbookInfo.wid.toString()
    }

    //API에서 단어 리스트를 가져옵니다.
    private fun getWords(){
        myPageViewModel.getWordbook(requireContext(), myPageViewModel.selectWordbookId)
    }

    private fun observeViewModel(){

        // 1. 단어 목록 로드 결과
        myPageViewModel.wordListResult.observe(viewLifecycleOwner) { response ->
            when(response){
                is ApiResponse.Success -> {
                    Log.d("log_mypage", "단어 불러오기 성공")
                    fragmentWordList.clear()

                    response.data.data.forEach { now ->
                        // API 데이터 -> UI 데이터 변환
                        fragmentWordList.add(
                            WordData(
                                now.wordId,
                                now.word,
                                now.meanings,
                                now.distractors,
                                now.example)
                        )
                    }

                    binding.vocNumTv.text = "${fragmentWordList.size}개"
                    adapter.notifyDataSetChanged()
                }
                is ApiResponse.Error -> {
                    Log.d("log_mypage", "단어 불러오기 실패: ${response.message}")
                }
            }
        }

        // 2. 단어장 자체 삭제 결과 관찰
        myPageViewModel.wordbookDeleteResult.observe(viewLifecycleOwner) { response ->

            if (response == null) return@observe

            when(response) {
                is ApiResponse.Success -> {
                    Toast.makeText(requireContext(), "단어장이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    // 단어장이 삭제되었으므로 목록 화면으로 이동
                    findNavController().popBackStack()
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), "삭제 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * RecyclerView와 어댑터를 초기화합니다.
     */
    private fun setupRecyclerView() {
        adapter = WordListAdapter(
            fragmentWordList,
            onItemClicked = { wordData, position -> },
            onTTSRequest = { }
        )

        binding.mypageWordListRecyclerview.adapter = adapter
        binding.mypageWordListRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        // RecyclerView에 리스너를 추가하여 자식 View가 생성될 때마다 개입합니다.
        binding.mypageWordListRecyclerview.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {

            // 이 함수는 각 아이템 View가 RecyclerView에 붙을 때 호출됩니다.
            override fun onChildViewAttachedToWindow(view: View) {
                val ttsButton = view.findViewById<View>(R.id.study_listen_imv)
                ttsButton?.visibility = View.GONE
            }

            // 이 함수는 아이템 View가 떨어져 나갈 때 호출됩니다.
            override fun onChildViewDetachedFromWindow(view: View) {}
        })
    }

    /**
     * 단어장 삭제 여부를 묻는 커스텀 다이얼로그를 띄웁니다.
     */
    private fun showDeleteVocDialog(vocName: String) {
        // 바인딩 생성 (XML과 일치)
        val dialogBinding = DialogCustomSelectBinding.inflate(layoutInflater)

        dialogBinding.dialogMessageTv.text = "${vocName}를 \n삭제하시겠습니까?"
        dialogBinding.dialogOkTv.text = "삭제"
        dialogBinding.dialogCancelTv.text = "취소"

        // 다이얼로그 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // 다이얼로그 투명
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // 취소 버튼 리스너
        dialogBinding.dialogCancelCdv.setOnClickListener {
            dialog.dismiss()
        }

        // 'OK' (추가) 버튼 리스너
        dialogBinding.dialogOkCdv.setOnClickListener {
            val wid = myPageViewModel.selectWordbookId.toString()
            var ownerUid = myPageViewModel.selectWordbookInfo.owner_uid

            // 만약 null이라면, UserPreference(내 정보)에서 가져오기
            if (ownerUid.isEmpty()) {
                ownerUid = userPreference.getUid().toString()
            }

            myPageViewModel.deleteWordbook(requireContext(), wid, ownerUid)

            dialog.dismiss()
        }

        dialog.show()
    }


    private fun copyID(){
        //1. Context에서 ClipboardManager 서비스 가져오기
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        //2. 복사할 TextView의 텍스트를 가져옵니다.
        val textToCopy = binding.vocIdTv.text.toString()

        //3. 텍스트를 ClipData 객체로 래핑
        //ClipData.newPlainText(레이블, 복사할 텍스트)
        val clip = ClipData.newPlainText("Copied Text", textToCopy)

        //4. 클립보드에 데이터 설정
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}