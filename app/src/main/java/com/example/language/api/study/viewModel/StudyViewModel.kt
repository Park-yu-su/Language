package com.example.language.api.study.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StudyViewModel : ViewModel() {
    val searchEventStart = MutableLiveData<Boolean>()
}