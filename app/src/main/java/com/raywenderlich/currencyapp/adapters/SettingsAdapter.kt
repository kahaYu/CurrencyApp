package com.raywenderlich.currencyapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.SettingsItemViewBinding
import com.raywenderlich.currencyapp.model.Rate

class SettingsAdapter(
    var ratesList: MutableList<Rate> = mutableListOf(),
    private val _onSwitchBoxListener: OnSwitchBoxListener,
    val startDragListener: OnStartDragListener
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    interface OnSwitchBoxListener {
        fun onSwitchBoxClicked(code: Int, state: Boolean)
    }

    interface OnStartDragListener {
        fun requestDrag(viewHolder: RecyclerView.ViewHolder)
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class SettingsViewHolder(
        val itemView: View,
        val binding: SettingsItemViewBinding,
        val listener: OnSwitchBoxListener?
    ) :
        RecyclerView.ViewHolder(itemView) {
        init {
            binding.apply {
                swVisibility.setOnClickListener {
                    val code = ratesList[adapterPosition].code
                    if (adapterPosition != RecyclerView.NO_POSITION) listener?.onSwitchBoxClicked(
                        code,
                        binding.swVisibility.isChecked
                    )
                }
                ivBurger.setOnLongClickListener { _ ->
                    startDragListener.requestDrag(this@SettingsViewHolder)
                    false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.settings_item_view, parent, false)
        val binding = SettingsItemViewBinding.bind(itemView)
        return SettingsViewHolder(itemView, binding, _onSwitchBoxListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.binding.apply {
            tvCurrencyEng.text = ratesList.get(position).iso
            tvCurrencyRus.text = ratesList.get(position).name
            tvQuantity.text = ratesList.get(position).quantity.toString()
            swVisibility.isChecked = ratesList.get(position).isChecked
        }
    }

    override fun getItemCount(): Int {
        return ratesList.size ?: 0
    }
}