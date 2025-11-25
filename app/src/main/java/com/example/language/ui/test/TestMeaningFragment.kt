package com.example.language.ui.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.language.R
import com.example.language.api.login.UserPreference
import com.example.language.api.test.TestRepository
import com.example.language.api.test.viewModel.TestViewModel
import com.example.language.api.test.viewModel.TestViewModelFactory
import com.example.language.data.WordData
import com.example.language.databinding.FragmentTestMeaningBinding
import com.example.language.ui.home.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TestMeaningFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TestMeaningFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTestMeaningBinding

    private lateinit var testWords : MutableList<WordData>
    private lateinit var nowTestWord : WordData

    private var nowWordIndex = 0
    private var totalWord = 0
    private var isLike = false

    //팝 애니메이션
    private lateinit var papAnim : Animation

    private val testRepository = TestRepository()
    private val testViewModel: TestViewModel by activityViewModels(){
        TestViewModelFactory(testRepository)
    }

    //유저 UID 가져오기
    private lateinit var userPreference : UserPreference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTestMeaningBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //상단 하단 제거
        (activity as? MainActivity)?.setUIVisibility(false)
        //애니메이션 로드
        papAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.button_pop)
        //userPref
        userPreference = UserPreference(requireContext())

        //먼저 값 불러오기
        getWordDataWithShuffle()

        //UI 세팅
        updateUI()
        binding.meanProgressbar.max = totalWord
        binding.meanNickanmeTv.text = userPreference.getName()



        //각 버튼 리스너
        binding.meanAnswer1Btn.setOnClickListener {
            binding.meanAnswer1Btn.startAnimation(papAnim)
            handleAnswer(binding.meanAnswer1Tv.text.toString(), 1)
        }
        binding.meanAnswer2Btn.setOnClickListener {
            binding.meanAnswer2Btn.startAnimation(papAnim)
            handleAnswer(binding.meanAnswer2Tv.text.toString(), 2)
        }
        binding.meanAnswer3Btn.setOnClickListener {
            binding.meanAnswer3Btn.startAnimation(papAnim)
            handleAnswer(binding.meanAnswer3Tv.text.toString(), 3)
        }
        binding.meanAnswer4Btn.setOnClickListener {
            binding.meanAnswer4Btn.startAnimation(papAnim)
            handleAnswer(binding.meanAnswer4Tv.text.toString(), 4)
        }

        binding.meanBackBtn.setOnClickListener {
            binding.meanBackBtn.startAnimation(papAnim)
            navigateToHome()

        }

        binding.meanLikeBtn.setOnClickListener {
            binding.meanLikeBtn.startAnimation(papAnim)
            handleLike()
        }
    }

    //일단 먼저 data를 한 번 섞을거야 그리고 개수도 맞추자.
    private fun getWordDataWithShuffle(){
        var tmpData = testViewModel.selectWordList
        var shuffleData = tmpData.shuffled()
        testWords = shuffleData.toMutableList()
        totalWord = testWords.size
    }
    
    //섞인 데이터셋에서 하나를 가져와 보기창을 만들기
    private fun makeQuiz(): List<String>{

        //일단 현재 테스트 할 거
        nowTestWord = testWords[nowWordIndex]
        //정답
        val correctAnswer = nowTestWord.meanings.get(0)
        //오답 3개
        val wrongAnswers = nowTestWord.distractors.shuffled().take(3)

        val options = mutableListOf<String>()
        options.add(correctAnswer)
        options.addAll(wrongAnswers)

        return options.shuffled()
    }

    //인자로 받는 값 1=review | 2=liked | 3=wrong
    private fun linkWordUser(status: Int){
        //일단 내 UID
        var stringUid = userPreference.getUid() ?: "0"
        var uid = stringUid.toInt()
        //타입 체크
        var typeCheck = listOf<String>("review", "liked")
        var type = typeCheck[status - 1]
        //단어 가져오기
        var wordIds = listOf<Int>(nowTestWord.wordId)

        testViewModel.linkWordUser(requireContext(), uid, wordIds, type)
    }
    private fun unlinkWordUser(status: Int){
        //일단 내 UID
        var stringUid = userPreference.getUid() ?: "0"
        var uid = stringUid.toInt()
        //타입 체크
        var typeCheck = listOf<String>("review", "liked", "wrong")
        var type = typeCheck[status - 1]
        //단어 가져오기
        var wordIds = listOf<Int>(nowTestWord.wordId)

        testViewModel.unlinkWordUser(requireContext(), uid, wordIds, type)
    }


    //홈 화면 이동
    fun navigateToHome(){
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    //좋아요 관리
    fun handleLike(){
        val likeBtn = binding.meanLikeBtn
        //안좋아
        if(!isLike){
            isLike = true
            likeBtn.setImageResource(R.drawable.ic_like_heart)
            linkWordUser(2) //like

        }
        else{
            isLike = false
            likeBtn.setImageResource(R.drawable.ic_like_heart2)
            unlinkWordUser(2) //unlike
        }

    }

    private fun handleAnswer(select: String, numBtn: Int){
        val correctAnswer = nowTestWord.meanings.get(0)

        //정답
        if(select == correctAnswer){
            showResult(true, numBtn)
        }
        else{
            showResult(false, numBtn)
        }
    }

    private fun showResult(check: Boolean, btnNum: Int){
        //1. 모양 맞추기
        var nowBtn = binding.meanAnswer1Btn
        var nowCheckBtn = binding.meanAnswer1Imv
        if(btnNum == 2){nowBtn = binding.meanAnswer2Btn
            nowCheckBtn = binding.meanAnswer2Imv}
        else if(btnNum == 3){nowBtn = binding.meanAnswer3Btn
            nowCheckBtn = binding.meanAnswer3Imv}
        else if(btnNum == 4){nowBtn = binding.meanAnswer4Btn
            nowCheckBtn = binding.meanAnswer4Imv}

        var chooseWord = nowTestWord.meanings.get(0)
        if(btnNum == 1){chooseWord = binding.meanAnswer1Tv.text.toString()}
        else if(btnNum == 2){chooseWord = binding.meanAnswer2Tv.text.toString()}
        else if(btnNum == 3){chooseWord = binding.meanAnswer3Tv.text.toString()}
        else if(btnNum == 4){chooseWord = binding.meanAnswer4Tv.text.toString()}

        binding.meanAnswer1Btn.isEnabled = false
        binding.meanAnswer2Btn.isEnabled = false
        binding.meanAnswer3Btn.isEnabled = false
        binding.meanAnswer4Btn.isEnabled = false


        //정답이면 바꾸기
        if(check){
            nowBtn.apply {
                strokeWidth = 2
                strokeColor = ContextCompat.getColor(requireContext(), R.color.Main1_1)
                //backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.Main1_5)
                nowCheckBtn.visibility = View.VISIBLE
                nowCheckBtn.setImageResource(R.drawable.ic_correct_blue)
            }
            //정답이니 오늘 푼 문제 추가
            linkWordUser(1) //review
            wrongHnadle(chooseWord)
        }
        //오답이면 바꾸기
        else{
            nowBtn.apply {
                strokeWidth = 2
                strokeColor = ContextCompat.getColor(requireContext(), R.color.redStroke)
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.redBackground)
                nowCheckBtn.visibility = View.VISIBLE
                nowCheckBtn.setImageResource(R.drawable.ic_correct_no_red)
            }
            //오답이니 틀린 문제 추가
            linkWordUser(1) //review
            //linkWordUser(3) //wrong
            wrongHnadle(chooseWord)
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            reResult(btnNum)
            nowWordIndex++
            updateUI()
        }


    }

    //푼 문제 API 호출하기
    private fun wrongHnadle(myAnswer: String){
        var uid = userPreference.getUid() ?: "0" //UID
        var wordId = nowTestWord.wordId //영단어 ID
        var wordText = nowTestWord.word //영단어
        var answer = nowTestWord.meanings.get(0) //정답 = 뜻
        var question = "choose the best meaning of this word. ${nowTestWord.example}"


        testViewModel.submitQuiz(requireContext(), uid.toInt(), wordId,
            wordText, question, myAnswer, answer)
    }

    private fun reResult(btnNum: Int){
        //1. 모양 맞추기
        var nowBtn = binding.meanAnswer1Btn
        var nowCheckBtn = binding.meanAnswer1Imv
        if(btnNum == 2){nowBtn = binding.meanAnswer2Btn
            nowCheckBtn = binding.meanAnswer2Imv}
        else if(btnNum == 3){nowBtn = binding.meanAnswer3Btn
            nowCheckBtn = binding.meanAnswer3Imv}
        else if(btnNum == 4){nowBtn = binding.meanAnswer4Btn
            nowCheckBtn = binding.meanAnswer4Imv}

        binding.meanAnswer1Btn.isEnabled = true
        binding.meanAnswer2Btn.isEnabled = true
        binding.meanAnswer3Btn.isEnabled = true
        binding.meanAnswer4Btn.isEnabled = true

        //이 이후에 버튼 or 시간 지나고 바꾸기
        nowBtn.apply {
            strokeWidth = 0
            backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.Main1_4)
            nowCheckBtn.visibility = View.INVISIBLE
        }
    }


    //단어 UI 관련 Handler
    private fun updateUI(){
        //아직 남으면 업뎃
        if(nowWordIndex < totalWord){
            //현재 문제 생성
            nowTestWord = testWords[nowWordIndex]
            //퀴즈 보기 생성
            val quizOptions = makeQuiz()

            //UI 적용
            binding.meanWordTv.text = nowTestWord.word
            binding.meanExampleTv.text = nowTestWord.example
            binding.meanProgressTv.text = "${nowWordIndex + 1}/${totalWord}"
            binding.meanProgressbar.progress = nowWordIndex + 1

            val answerButtons = listOf(
                binding.meanAnswer1Tv,
                binding.meanAnswer2Tv,
                binding.meanAnswer3Tv,
                binding.meanAnswer4Tv
            )
            quizOptions.forEachIndexed { index, text ->
                answerButtons[index].text = text
            }

        }
        //끝나면
        else{
            Toast.makeText(context, "테스트 완료!", Toast.LENGTH_LONG).show()
            navigateToHome()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.setUIVisibility(true)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TestMeaningFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TestMeaningFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}