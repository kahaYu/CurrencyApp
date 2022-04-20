package com.raywenderlich.currencyapp.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.adapters.CurrencyAdapter
import com.raywenderlich.currencyapp.databinding.FragmentCurrencyBinding
import com.raywenderlich.currencyapp.utils.AutoClearedValue
import com.raywenderlich.currencyapp.utils.Resource
import com.raywenderlich.currencyapp.utils.Transmitter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CurrencyFragment: Fragment() {

    private var binding by AutoClearedValue<FragmentCurrencyBinding>(this)

    private val vm by activityViewModels<MainViewModel>()

    private lateinit var currencyAdapter: CurrencyAdapter

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

        setupRecyclerView()

        vm.currencies.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { currensiesResponse ->
                        currencyAdapter.responseList = currensiesResponse
                        currencyAdapter.notifyDataSetChanged()
                        //updateScrollView()
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        showSafeToast(view, message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    fun navigateToSettingsFragment () {
            lifecycleScope.launch { // Some delay to let animation of button play
            delay(210L)
            findNavController().navigate(R.id.action_currencyFragment_to_settingsFragment)
        }
    }

    fun navigateBack () {
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

    private fun showSafeToast(view: View, text: String) { // Prevent appearance of multiply toasts at once
        if (Calendar.getInstance().timeInMillis >= vm.toastShowTime + 4000L) {
            val snackbar = Snackbar.make(view, "Ошибка: $text", Snackbar.LENGTH_LONG)
            //snackbar.view.background = resources.getDrawable(R.drawable.snackbar_background)
            snackbar.show()
            vm.toastShowTime = Calendar.getInstance().timeInMillis
        }
    }

    private fun setupRecyclerView() {
        currencyAdapter = CurrencyAdapter()
        binding.recyclerView.apply {
            adapter = currencyAdapter
            layoutManager = LinearLayoutManager(activity)
            //addOnScrollListener(this@BreakingNewsFragment.scrollListener)
            setHasFixedSize(true) // Accelerate work of rv
        }
    }

    //private fun updateScrollView() {
    //    val screenHeight = resources.displayMetrics.run { heightPixels / density }
    //    val recyclerViewHeight = 48 * (currencyAdapter.responseList?.size ?: 0)
    //    val placeHolderHeight =
    //        if (recyclerViewHeight < screenHeight.toInt() - 72 - 24) // Check if RV occupy all visible area
    //        screenHeight - recyclerViewHeight - 72 - 24
    //        else 0
    //    binding.bottomPlaceholder.layoutParams.height = placeHolderHeight.toInt() * resources.displayMetrics.density.toInt()
//
    //    binding.scrollView.post {
    //        binding.scrollView.scrollTo(0, binding.recyclerView.top)
    //    }
    //    binding.scrollView.isSmoothScrollingEnabled = true
    //}
}