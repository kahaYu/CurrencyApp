package com.raywenderlich.currencyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.FragmentCurrencyBinding
import com.raywenderlich.currencyapp.databinding.FragmentSettingsBinding
import com.raywenderlich.currencyapp.utils.AutoClearedValue

class SettingsFragment: Fragment() {

    private var binding by AutoClearedValue<FragmentSettingsBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragment = this
    }

    fun navigateBack () {
        findNavController().navigateUp()
    }
}