package com.raywenderlich.currencyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.currencyapp.adapters.SettingsAdapter
import com.raywenderlich.currencyapp.databinding.FragmentSettingsBinding
import com.raywenderlich.currencyapp.utils.AutoClearedValue
import com.raywenderlich.currencyapp.utils.getCurrency
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

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

    override fun onDestroy() {
        super.onDestroy()
        vm.modifyedRatesState.clear()
        vm.modifyedRatesOrder.clear()
    }

    fun navigateBack() {
        lifecycleScope.launch { // Some delay to let animation of button play
            delay(210L)
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        vm.todaysResponseBodyOrdered.forEach { vm.modifyedRatesOrder.add(it) }
        val dragDropCallback =
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP
                        or ItemTouchHelper.DOWN
                        or ItemTouchHelper.START
                        or ItemTouchHelper.END,
                0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = viewHolder.adapterPosition
                    val toPosition = target.adapterPosition

                    Collections.swap(vm.modifyedRatesOrder, fromPosition, toPosition)

                    settingsAdapter.notifyItemMoved(fromPosition, toPosition)
                    return true
                }

                override fun isLongPressDragEnabled(): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            }

        val itemTouchHelper = ItemTouchHelper(dragDropCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        val onSwitchBoxListener = object : SettingsAdapter.OnSwitchBoxListener {
            override fun onSwitchBoxClicked(code: Int, state: Boolean) {
                // 8. При изменении видимости в настройках, создаём новый список Р изменённых элементов.
                if (!vm.modifyedRatesState.contains(vm.todaysResponseBodyOrdered.getCurrency(code)!!))
                    vm.modifyedRatesState.add(
                        vm.todaysResponseBodyOrdered.getCurrency(code)!!.copy()
                    )
                vm.modifyedRatesState.getCurrency(code)?.isChecked = state
            }
        }
        val onStartDragListener = object : SettingsAdapter.OnStartDragListener {
            override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
                itemTouchHelper.startDrag(viewHolder)
            }
        }

        // 7. Во фрагменте настроек назначаем адаптеру список Х.
        settingsAdapter =
            SettingsAdapter(vm.todaysResponseBodyOrdered, onSwitchBoxListener, onStartDragListener)
        binding.recyclerView.apply {
            adapter = settingsAdapter
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true) // Accelerate work of rv
        }
    }
}