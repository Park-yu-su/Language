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
import androidx.lifecycle.lifecycleScope
import com.example.language.R
import com.example.language.data.WordData
import com.example.language.databinding.FragmentTestMeaningBinding
import com.example.language.ui.home.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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


    //임시 데이터
    private var tmpData = mutableListOf(
        WordData("APPLE", mutableListOf("사과", "사과2", "사과3", "사과4"), "An apple a day keeps the doctor away."),
        WordData("EFFICIENT", mutableListOf("효율적인", "사과2", "사과3", "사과4"), "We need an efficient solution."),
        WordData("PROGRAMMING", mutableListOf("프로그래밍", "사과2", "사과3", "사과4"), "I love programming."),
        WordData("LANGUAGE", mutableListOf("언어", "사과2", "사과3", "사과4"), "English is a global language."),
        WordData("DEVELOPMENT", mutableListOf("개발", "사과2", "사과3", "사과4"), "Software development is complex.")
    )
    private var nowWordIndex = 0
    private var totalWord = tmpData.size

    //팝 애니메이션
    private lateinit var papAnim : Animation


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

        //UI 세팅
        updateUI()
        binding.meanProgressbar.max = totalWord

        //각 버튼 리스너
        binding.meanAnswer1Btn.setOnClickListener {
            binding.meanAnswer1Btn.startAnimation(papAnim)
            handleAnswer(binding.meanAnswer1Btn.text.toString())
        }
        binding.meanAnswer2Btn.setOnClickListener {
            binding.meanAnswer2Btn.startAnimation(papAnim)
            handleAnswer(binding.meanAnswer2Btn.text.toString())
        }
        binding.meanAnswer3Btn.setOnClickListener {
            binding.meanAnswer3Btn.startAnimation(papAnim)
            handleAnswer(binding.meanAnswer3Btn.text.toString())
        }
        binding.meanAnswer4Btn.setOnClickListener {
            binding.meanAnswer4Btn.startAnimation(papAnim)
            handleAnswer(binding.meanAnswer4Btn.text.toString())
        }

        binding.meanBackBtn.setOnClickListener {
            binding.meanBackBtn.startAnimation(papAnim)
            navigateToHome()

        }
    }

    //홈 화면 이동
    fun navigateToHome(){
        requireActivity().onBackPressed()
    }

    private fun handleAnswer(select: String){
        //정답
        if(select == tmpData.get(nowWordIndex).meanings.get(0)){
            showAnimate(true)
        }
        else{
            showAnimate(false)
        }
    }

    private fun showAnimate(check: Boolean){
        val imgCheck = binding.meanCheckAnswerImv
        //1. 모양 맞추기
        if(check){
            imgCheck.setImageResource(R.drawable.ic_correct_o)
        }
        else{
            imgCheck.setImageResource(R.drawable.ic_correct_x)
        }
        //2. 초기 상태
        imgCheck.visibility = View.VISIBLE
        imgCheck.alpha = 0f
        imgCheck.scaleX = 0.5f
        imgCheck.scaleY = 0.5f

        //3. 애니메이션 실행
        imgCheck.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                //4. 애니메이션 종료 후 코루틴을 사용하여 딜레이 처리
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)
                    imgCheck.visibility = View.INVISIBLE

                    if(check){
                        nowWordIndex++
                        updateUI()
                    }
                }
            }
            .start()

    }


    //단어 UI 관련 Handler
    private fun updateUI(){
        //아직 남으면 업뎃
        if(nowWordIndex < totalWord){
            var nowWord = tmpData.get(nowWordIndex)

            //UI 적용
            binding.meanWordTv.text = nowWord.word
            binding.meanExampleTv.text = nowWord.example
            binding.meanProgressTv.text = "${nowWordIndex + 1}/${totalWord}"
            binding.meanProgressbar.progress = nowWordIndex + 1

            binding.meanAnswer1Btn.text = nowWord.meanings.get(0)
            binding.meanAnswer2Btn.text = nowWord.meanings.get(1)
            binding.meanAnswer3Btn.text = nowWord.meanings.get(2)
            binding.meanAnswer4Btn.text = nowWord.meanings.get(3)



        }
        //끝나면
        else{
            Toast.makeText(context, "테스트 완료!", Toast.LENGTH_LONG).show()
            

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