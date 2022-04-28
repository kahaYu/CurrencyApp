package com.raywenderlich.currencyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.CurrencyItemViewBinding
import com.raywenderlich.currencyapp.model.Rate
import com.raywenderlich.currencyapp.utils.format

class CurrencyAdapter : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Rate>() {
        override fun areItemsTheSame(oldItem: Rate, newItem: Rate): Boolean {
            return oldItem.iso == newItem.iso
        }
        override fun areContentsTheSame(oldItem: Rate, newItem: Rate): Boolean {
            return oldItem == newItem
        }
    }

    //var responseList: List<Rate>? = null
    val differ = AsyncListDiffer(this, differCallback)

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
        val rate = differ.currentList[position]
        holder.binding.apply {
            tvCurrencyEng.text = rate.iso
            tvCurrencyRus.text = rate.name
            tvCurrencyToday.text = rate.rate.format(4)
            tvQuantity.text = rate.quantity.toString()
            tvCurrencyTomorrow.text = rate.rateTomorrow.format(4)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size ?: 0
    }
}