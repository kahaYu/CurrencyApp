package com.raywenderlich.currencyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.CurrencyItemViewBinding
import com.raywenderlich.currencyapp.model.CombinedResponse
import com.raywenderlich.currencyapp.model.NationalRateListResponse
import com.raywenderlich.currencyapp.model.Rate
import com.raywenderlich.currencyapp.utils.format

class CurrencyAdapter : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    var responseList: List<Rate>? = null

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
            tvCurrencyEng.text = responseList?.get(position)?.iso
            tvCurrencyRus.text = responseList?.get(position)?.name
            tvCurrencyToday.text = responseList?.get(position)?.rate?.format(4)
            tvQuantity.text = responseList?.get(position)?.quantity.toString()
            tvCurrencyTomorrow.text = responseList?.get(position)?.rateTomorrow?.format(4)
        }
    }

    override fun getItemCount(): Int {
        return responseList?.size ?: 0
    }
}