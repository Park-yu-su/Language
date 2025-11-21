package com.example.language.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.api.ApiResponse
import com.example.language.api.WordDataWithWordID
import com.example.language.api.login.UserPreference
import com.example.language.api.study.StudyRepository
import com.example.language.api.study.viewModel.StudyViewModel
import com.example.language.api.study.viewModel.StudyViewModelFactory
import com.example.language.databinding.FragmentHomeBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import kotlin.getValue
import kotlin.toString

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userPreference: UserPreference

    private lateinit var textToSpeech: TextToSpeech

    //달력 관련
    private lateinit var selectedDate: CalendarDay
    private lateinit var todayDate : LocalDate
    private lateinit var todayDec: TodayDecorator
    private lateinit var selectedDec: SelectedDecorator
    private lateinit var sundayDec: SundayDecorator

    //API 연결을 위한 수단
    private val studyRepository = StudyRepository()
    private val studyViewModel: StudyViewModel by activityViewModels(){
        StudyViewModelFactory(studyRepository)
    }
    private var todayQuizAnswer : Boolean = false


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
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //색깔 정의
        (activity as MainActivity).setTopBar("찍어보카", false, true)
        (activity as MainActivity).showToprightIcon(false, 1)
        (activity as MainActivity).setUIVisibility(true)
        userPreference = UserPreference(requireContext())

        binding.homeNameTv.text = userPreference.getName()

        //달력 초기화
        todayDec = TodayDecorator(requireContext())
        sundayDec   = SundayDecorator(requireContext())
        selectedDec = SelectedDecorator(requireContext())
        initCalendar()

        //TTS 세팅
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }

        //그 후 화면 세팅을 위한 단어 가져오기
        observeRandomWord()
        var stringUid = userPreference.getUid() ?: "0"
        var uid = stringUid.toInt()
        studyViewModel.getRandomWord(requireContext(), uid)


        //단어 테스트 로직
        binding.homeTestBtn.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_homeFragment_to_testFragment)


        }

        //단어장 만들기 로직
        binding.homeMakeVocBtn.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_homeFragment_to_makeVocFragment)
        }

        //단어 발음 로직
        binding.homeTodaywordListenBtn.setOnClickListener {
            val nowWord = binding.homeTodaywordEnglishTv.text.toString()
            binding.homeTodaywordListenBtn.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.button_pop))

            textToSpeech.speak(nowWord, TextToSpeech.QUEUE_FLUSH, null, null)
        }


        //퀴즈 버튼 리스너 달기
        binding.homeQuizTrueBtn.setOnClickListener {
            handleTodayQuiz(true)
        }
        binding.homeQuizFalseBtn.setOnClickListener {
            handleTodayQuiz(false)
        }

    }

    //랜덤 단어 observe
    private fun observeRandomWord(){
        studyViewModel.randomWordListResult.observe(viewLifecycleOwner) { response ->
            when(response){
                is ApiResponse.Success -> {
                    Log.d("log_study", "랜덤 단어 불러오기 성공 : ${response.data}")
                    var words = response.data.data

                    //여기서 랜덤으로 2개 뽑아서 처리하자
                    if(words.size > 1) {
                        var randomWord1 = words.random()
                        var randomWord2 = words.random()
                        Log.d("log_study", "랜덤 단어 1 : ${randomWord1}")
                        Log.d("log_study", "랜덤 단어 2 : ${randomWord2}")

                        showTodayWord(randomWord1)
                        showhandleQuiz(randomWord2)
                    }
                }
                is ApiResponse.Error -> {
                    Log.d("log_study", "랜덤 단어 불러오기 실패 : ${response.message}")

                }
            }
        }
    }


    //랜덤 단어 페이지 보여주기
    private fun showTodayWord(word: WordDataWithWordID){
        binding.homeTodaywordEnglishTv.text = word.word
        binding.homeTodaywordKoearnTv.text = word.meanings.get(0)
        binding.homeTodaywordExampleTv.text = word.example
    }

    //quiz process 정의
    private fun showhandleQuiz(word: WordDataWithWordID){
        //퀴즈 질문 생성
        binding.homeQuizExampleTv.text = word.example
        var randNum = (0..1).random()
        var quiz = ""
        if(randNum == 0){
            todayQuizAnswer = true
            quiz += "아래 예문에서 '${word.word}'는 '${word.meanings.get(0)}'라는 의미로 사용되었다."
        }
        else{
            todayQuizAnswer = false
            quiz += "아래 예문에서 '${word.word}'는 '${word.distractors.get((0..2).random())}'라는 의미로 사용되었다."
        }
        binding.homeQuizQuestionTv.text = quiz


    }
    private fun handleTodayQuiz(choose: Boolean){
        val rightStrokeColorInt = ContextCompat.getColor(requireContext(), R.color.Main1_1)
        val rightBackgroundColorInt = ContextCompat.getColor(requireContext(), R.color.Main1_5)

        val wrongStrokeColorInt = ContextCompat.getColor(requireContext(), R.color.redStroke)
        val wrongBackgroundColorInt = ContextCompat.getColor(requireContext(), R.color.redBackground)

        //정답
        if(todayQuizAnswer == choose) {
            if(choose){
                binding.homeQuizTrueBtn.strokeColor = ColorStateList.valueOf(rightStrokeColorInt)
                binding.homeQuizTrueBtn.backgroundTintList = ColorStateList.valueOf(rightBackgroundColorInt)

            }
            else{
                binding.homeQuizFalseBtn.strokeColor = ColorStateList.valueOf(rightStrokeColorInt)
                binding.homeQuizFalseBtn.backgroundTintList = ColorStateList.valueOf(rightBackgroundColorInt)
            }
        }
        //오답
        else{
            if(choose){
                binding.homeQuizTrueBtn.strokeColor = ColorStateList.valueOf(wrongStrokeColorInt)
                binding.homeQuizTrueBtn.backgroundTintList = ColorStateList.valueOf(wrongBackgroundColorInt)
            }
            else{
                binding.homeQuizFalseBtn.strokeColor = ColorStateList.valueOf(wrongStrokeColorInt)
                binding.homeQuizFalseBtn.backgroundTintList = ColorStateList.valueOf(wrongBackgroundColorInt)
            }
        }
        //버튼 비활성화
        binding.homeQuizTrueBtn.isEnabled = false
        binding.homeQuizFalseBtn.isEnabled = false
    }


    //달력 초기화
    private fun initCalendar(){

        //1. 달력 초기화
        val cal = binding.homeCalendar

        //일요일 시작
        cal.state().edit()
            .setFirstDayOfWeek(DayOfWeek.SUNDAY)
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        //오늘 날짜 표시 및 오늘 날짜 저장(막기 용도)
        cal.setCurrentDate(CalendarDay.today())
        todayDate = LocalDate.now()

        //달 표시
        val headerFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        cal.setTitleFormatter { day: CalendarDay ->
            day.date.format(headerFmt)
        }

        //주 표시
        cal.setWeekDayFormatter { dow: DayOfWeek ->
            val labels = listOf("S","M","T","W","T","F","S")
            // DayOfWeek.value: MONDAY=1…SUNDAY=7 → 7%7=0 으로 Sunday가 맨 앞
            val idx = dow.value % 7
            val txt = labels[idx]
            if (dow == DayOfWeek.SUNDAY) {
                //spannable로 텍스트 일부에 접근해 변경 가능
                SpannableString(txt).apply {
                    setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(requireContext(), R.color.redStroke)
                        ), 0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            } else txt
        }

        //2. 데코레이터 적용 및 날짜 선택 리스너 설정
        cal.apply {
            // 한 번만 add!
            addDecorators(todayDec, selectedDec, sundayDec)
            //addDecorators(monthDec)

            //특정 날짜를 눌렀을 때 반응
            setOnDateChangedListener { _, date, selected ->
                if (selected) {
                    //0. 오늘 이후의 날짜면 처리 X
                    if(date.date.isAfter(todayDate)){
                        return@setOnDateChangedListener
                    }
                    //1. 직접 보관한 selectedDec 에 선택 날짜를 넘기고
                    selectedDec.setSelected(date)
                    //2. 달력 리프레시
                    invalidateDecorators()
                    //3. 로그 출력
                    Log.d("log_calendar", date.toString())
                }
            }

            //이번에는 월이 바뀌었을 때 반응
            setOnMonthChangedListener { _, date ->
                //api 호출


            }
        }


        //3. 오늘 날짜 get 및 상세 페이지에 넣기
        selectedDate = CalendarDay.today()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}