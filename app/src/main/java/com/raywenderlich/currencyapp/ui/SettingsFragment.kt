package com.raywenderlich.currencyapp.ui

import android.os.Bundle
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.currencyapp.adapters.SettingsAdapter
import com.raywenderlich.currencyapp.databinding.FragmentSettingsBinding
import com.raywenderlich.currencyapp.model.Rate
import com.raywenderlich.currencyapp.utils.AutoClearedValue
import com.raywenderlich.currencyapp.utils.getCurrency
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

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
        binding.vm = vm

        setupRecyclerView()

    }

    fun navigateBack() {
        lifecycleScope.launch { // Some delay to let animation of button play
            delay(210L)
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        // 7. Во фрагменте настроек назначаем адаптеру список Х.
        settingsAdapter = SettingsAdapter(vm.todaysResponseBodyOrdered)
        binding.recyclerView.apply {
            adapter = settingsAdapter
            layoutManager = LinearLayoutManager(activity)
            //addOnScrollListener(this@BreakingNewsFragment.scrollListener)
            setHasFixedSize(true) // Accelerate work of rv
        }
        val listener = object : SettingsAdapter.onSwitchBoxListener {
            override fun onSwitchBoxClicked(code: Int, state: Boolean) {
                // 8. При изменении видимости в настройках, создаём новый список Р изменённых элементов.
                if (!vm.modifyedCurrencies.contains(vm.todaysResponseBodyOrdered.getCurrency(code)!!))
                    vm.modifyedCurrencies.add(vm.todaysResponseBodyOrdered.getCurrency(code)!!.copy())
                vm.modifyedCurrencies.getCurrency(code)?.isChecked = state
            }
        }
        settingsAdapter.setOnSwitchBoxListener(listener)
    }

    //private fun changeState (code: Int) {
    //    settingsAdapter.ratesList.apply {
    //        changeState(code, !this.getCurrency(code)?.isChecked!!)
    //    }
    //}
}