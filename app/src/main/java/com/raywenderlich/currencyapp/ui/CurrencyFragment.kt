package com.raywenderlich.currencyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.FragmentCurrencyBinding
import com.raywenderlich.currencyapp.utils.AutoClearedValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CurrencyFragment : Fragment() {

    private var binding by AutoClearedValue<FragmentCurrencyBinding>(this)

    private var jobNavigateToSettingsFragment: Job? = null
    private var jobNavigateBack: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragment = this


    }

    override fun onPause() {
        super.onPause()
        jobNavigateToSettingsFragment?.cancel()
        jobNavigateBack?.cancel()

    }

    fun navigateToSettingsFragment () {
        jobNavigateToSettingsFragment = MainScope().launch { // Some delay to let animation of button play
            delay(210L)
            findNavController().navigate(R.id.action_currencyFragment_to_settingsFragment)
        }
    }

    fun navigateBack () {
        jobNavigateBack = MainScope().launch { // Some delay to let animation of button play
            delay(210L)
            requireActivity().moveTaskToBack(true)
        }
    }
}