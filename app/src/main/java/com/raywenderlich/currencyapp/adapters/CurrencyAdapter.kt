package com.raywenderlich.currencyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.CurrencyItemViewBinding
import com.raywenderlich.currencyapp.model.CombinedResponse
import com.raywenderlich.currencyapp.model.NationalRateListResponse

class CurrencyAdapter : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    var responseList: CombinedResponse? = null

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
            tvCurrencyEng.text = responseList?.todayResponse?.get(position)?.iso
            tvCurrencyRus.text = responseList?.todayResponse?.get(position)?.name
            tvCurrencyToday.text = responseList?.todayResponse?.get(position)?.rate.toString()
            tvQuantity.text = responseList?.todayResponse?.get(position)?.quantity.toString()
            tvCurrencyTomorrow.text = responseList?.tomorrowResponse?.get(position)?.toString()
        }
    }

    override fun getItemCount(): Int {
        return responseList?.todayResponse?.size ?: 0
    }
}