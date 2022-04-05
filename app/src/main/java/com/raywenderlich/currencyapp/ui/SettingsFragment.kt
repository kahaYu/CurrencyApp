package com.raywenderlich.currencyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.adapters.CurrencyAdapter
import com.raywenderlich.currencyapp.adapters.SettingsAdapter
import com.raywenderlich.currencyapp.databinding.FragmentCurrencyBinding
import com.raywenderlich.currencyapp.databinding.FragmentSettingsBinding
import com.raywenderlich.currencyapp.utils.AutoClearedValue
import com.raywenderlich.currencyapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment: Fragment() {

    private var binding by AutoClearedValue<FragmentSettingsBinding>(this)

    private val vm by activityViewModels<MainViewModel>()

    private lateinit var settingsAdapter: SettingsAdapter

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

        setupRecyclerView()

        settingsAdapter.ratesList = vm.ratesOrderedSettingsScreen
        settingsAdapter.notifyDataSetChanged()


    }

    fun navigateBack () {
        lifecycleScope.launch { // Some delay to let animation of button play
            delay(210L)
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        settingsAdapter = SettingsAdapter()
        binding.recyclerView.apply {
            adapter = settingsAdapter
            layoutManager = LinearLayoutManager(activity)
            //addOnScrollListener(this@BreakingNewsFragment.scrollListener)
            setHasFixedSize(true) // Accelerate work of rv
        }
    }
}