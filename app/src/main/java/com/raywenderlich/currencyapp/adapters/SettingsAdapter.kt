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

class SettingsAdapter : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    var ratesList: List<Rate>? = null

    class SettingsViewHolder(itemView: View, val binding: SettingsItemViewBinding) :
        RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).
            inflate(R.layout.settings_item_view, parent, false)
        val binding = SettingsItemViewBinding.bind(itemView)
        return SettingsViewHolder(itemView, binding)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.binding.apply {
            tvCurrencyEng.text = ratesList?.get(position)?.iso
            tvCurrencyRus.text = ratesList?.get(position)?.name
            tvQuantity.text = ratesList?.get(position)?.quantity.toString()
            swVisibility.isChecked = ratesList?.get(position)?.isChecked!!
        }
    }

    override fun getItemCount(): Int {
        return ratesList?.size ?: 0
    }
}