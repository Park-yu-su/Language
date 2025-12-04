package com.example.language.ui.home

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.language.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

// 사용자가 터치한 날짜에 대해 표시
class SelectedDecorator(private val context: Context) : DayViewDecorator {
    private var selectedDay: CalendarDay? = null
    private val today = CalendarDay.today()
    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_calendar_select)!!

    //내가 직접 선택한 날짜를 전달
    fun setSelected(day: CalendarDay) {
        selectedDay = day
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return (day == selectedDay) && (today != day)
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(icon)

    }
}