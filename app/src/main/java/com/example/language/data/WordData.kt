package com.example.language.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class WordData (
    var wordId : Int,
    var word : String,
    val meanings: List<String>,
    val distractors: List<String>,
    val example: String
) : Parcelable