package com.raywenderlich.currencyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.ActivityMainBinding.inflate
import com.raywenderlich.currencyapp.databinding.CurrencyItemViewBinding
import com.raywenderlich.currencyapp.databinding.SettingsItemViewBinding
import com.raywenderlich.currencyapp.model.Rate

class SettingsAdapter (var ratesList: MutableList<Rate> = mutableListOf()): RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    private var listener: onSwitchBoxListener? = null

    interface onSwitchBoxListener {
        fun onSwitchBoxClicked(code: Int, state: Boolean)
    }

    fun setOnSwitchBoxListener(_listener: onSwitchBoxListener) {
        listener = _listener
    }

    inner class SettingsViewHolder(
        val itemView: View,
        val binding: SettingsItemViewBinding,
        val listener: onSwitchBoxListener?
    ) :
        RecyclerView.ViewHolder(itemView) {
        init {
            binding.swVisibility.setOnClickListener {
                var code = ratesList[adapterPosition].code
                if (adapterPosition != RecyclerView.NO_POSITION) listener?.onSwitchBoxClicked(code, binding.swVisibility.isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.settings_item_view, parent, false)
        val binding = SettingsItemViewBinding.bind(itemView)
        return SettingsViewHolder(itemView, binding, listener)
    }

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