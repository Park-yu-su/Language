package com.example.language.ui.home

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import com.example.language.R
import com.example.language.api.login.UserPreference
import com.example.language.databinding.FragmentHomeBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
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


    //달력 관련
    private lateinit var selectedDate: CalendarDay
    private lateinit var todayDate : LocalDate
    private lateinit var todayDec: TodayDecorator
    private lateinit var selectedDec: SelectedDecorator
    private lateinit var sundayDec: SundayDecorator

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


        //단어 테스트 로직
        binding.homeTestBtn.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_homeFragment_to_testFragment)


        }

        // 단어장 만들기 로직
        binding.homeMakeVocBtn.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_homeFragment_to_makeVocFragment)
        }

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