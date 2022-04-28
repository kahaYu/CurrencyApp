package com.raywenderlich.currencyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.adapters.CurrencyAdapter
import com.raywenderlich.currencyapp.databinding.FragmentCurrencyBinding
import com.raywenderlich.currencyapp.databinding.ObnovitViewBinding

import com.raywenderlich.currencyapp.utils.AutoClearedValue
import com.raywenderlich.currencyapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class CurrencyFragment : Fragment() {

    private var binding by AutoClearedValue<FragmentCurrencyBinding>(this)
    private var bindingObnovit by AutoClearedValue<ObnovitViewBinding>(this)

    private val vm by activityViewModels<MainViewModel>()

    private lateinit var currencyAdapter: CurrencyAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var obnovitView: LinearLayout

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
        binding.lifecycleOwner = this
        binding.vm = vm

        setupScrollView()
        setupRecyclerView()
        setupObnovitView()

        vm.currencies.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.btSettings.visibility = View.VISIBLE
                    when (binding.scrollView.getChildAt(1)) {
                        is RecyclerView -> {
                        }
                        is LinearLayout -> {
                            binding.scrollView.apply {
                                mTarget = null
                                removeView(binding.scrollView.getChildAt(1))
                                addView(recyclerView)
                                if (!vm.isNavigatedFromSettingsFragment) vm.smoothAppearance(
                                    recyclerView,
                                    1F
                                )
                            }
                        }
                        null -> {
                            binding.scrollView.addView(recyclerView)
                            if (!vm.isNavigatedFromSettingsFragment) vm.smoothAppearance(
                                recyclerView,
                                1F
                            )
                        }
                    }
                    hideProgressBar()
                    response.data?.let { currenciesResponse ->
                        currencyAdapter.differ.submitList(currenciesResponse)
                    }
                }
                is Resource.Error -> {
                    binding.btSettings.visibility = View.GONE
                    when (binding.scrollView.getChildAt(1)) {
                        is LinearLayout -> {
                        }
                        is RecyclerView -> {
                        }
                        null -> {
                            binding.scrollView.addView(bindingObnovit.root)
                            if (!vm.isNavigatedFromSettingsFragment) vm.smoothAppearance(
                                bindingObnovit.root,
                                0.15F
                            )
                        }
                    }
                    hideProgressBar()
                    response.message?.let { message ->
                        showSafeToast(view, message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
            vm.isNavigatedFromSettingsFragment = false
            binding.scrollView.setRefreshing(false)
        })
    }

    fun navigateToSettingsFragment() {
        lifecycleScope.launch { // Some delay to let animation of button play
            delay(210L)
            findNavController().navigate(R.id.action_currencyFragment_to_settingsFragment)
        }
    }

    fun navigateBack() {
        lifecycleScope.launch { // Some delay to let animation of button play
            delay(210L)
            requireActivity().moveTaskToBack(true)
        }
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showSafeToast(
        view: View,
        text: String
    ) { // Prevent appearance of multiply toasts at once
        if (Calendar.getInstance().timeInMillis >= vm.toastShowTime + 4000L) {
            val snackbar = Snackbar.make(view, "Ошибка: $text", Snackbar.LENGTH_LONG)
            snackbar.show()
            vm.toastShowTime = Calendar.getInstance().timeInMillis
        }
    }

    private fun setupRecyclerView() {
        currencyAdapter = CurrencyAdapter()
        recyclerView = LayoutInflater.from(context)
            .inflate(R.layout.recycler_view, binding.root as ViewGroup, false) as RecyclerView
        (recyclerView as RecyclerView).apply {
            adapter = currencyAdapter
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true) // Accelerate work of rv
        }
    }

    private fun setupScrollView() {
        binding.progressBar.start() // Start main progress bar
        binding.scrollView.apply {
            setRefreshListener {
                vm.isRefreshing = true
                vm.getCurrencies()
            }
        }
    }

    private fun setupObnovitView() {
        obnovitView = LayoutInflater.from(context)
            .inflate(R.layout.obnovit_view, binding.root as ViewGroup, false) as LinearLayout
        bindingObnovit = ObnovitViewBinding.bind(obnovitView)
        vm.startArrowAnimation(bindingObnovit.icArrowDown)
    }
}