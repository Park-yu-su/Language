package com.example.language.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class WordData (
    var word : String,
    var meanings : MutableList<String>,
    var example: String
) : Parcelable