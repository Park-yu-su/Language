package com.example.language.ui.makeVoc

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class SelectWayAddVocFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = SelectWayAddVocFragmentArgs.fromBundle(requireArguments())
        val vocName = args.vocName
    }
}