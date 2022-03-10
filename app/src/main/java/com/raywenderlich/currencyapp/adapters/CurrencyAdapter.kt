package com.raywenderlich.currencyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.ActivityMainBinding.inflate
import com.raywenderlich.currencyapp.databinding.CurrencyItemViewBinding
import com.raywenderlich.currencyapp.model.CurrenciesResponse

class CurrencyAdapter : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    var list: CurrenciesResponse? = null

    class CurrencyViewHolder(itemView: View, val binding: CurrencyItemViewBinding) :
        RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).
            inflate(R.layout.currency_item_view, parent, false)
        val binding = CurrencyItemViewBinding.bind(itemView)
        return CurrencyViewHolder(itemView, binding)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        holder.binding.apply {
            tvCurrencyEng.text = list?.get(position)?.Cur_Abbreviation
            tvCurrencyRus.text = list?.get(position)?.Cur_Name
            tvCurrencyToday.text = list?.get(position)?.Cur_OfficialRate.toString()
            tvCurrencyTomorrow.text = "123"
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }
}