package com.raywenderlich.currencyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.ActivityMainBinding.inflate
import com.raywenderlich.currencyapp.databinding.CurrencyItemViewBinding
import com.raywenderlich.currencyapp.databinding.SettingsItemViewBinding

//class SettingsAdapter : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {
//
//    val list = ArrayList<CurrencySetting>()
//
//    class SettingsViewHolder(itemView: View, val binding: SettingsItemViewBinding) :
//        RecyclerView.ViewHolder(itemView)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
//        val itemView =
//            LayoutInflater.from(parent.context).
//            inflate(R.layout.settings_item_view, parent, false)
//        val binding = SettingsItemViewBinding.bind(itemView)
//        return SettingsViewHolder(itemView, binding)
//    }
//
//    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
//        holder.binding.apply {
//            tvCurrencyEng.text = list[position].nameEng
//            tvCurrencyRus.text = list[position].nameRus
//            swVisibility.isChecked = list[position].isChecked
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return list.size
//    }
//}