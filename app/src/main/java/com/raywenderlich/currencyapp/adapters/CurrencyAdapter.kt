package com.raywenderlich.currencyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.CurrencyItemViewBinding
import com.raywenderlich.currencyapp.databinding.PlaceholderItemViewBinding
import com.raywenderlich.currencyapp.model.Rate
import com.raywenderlich.currencyapp.utils.format

private val ITEM_VIEW_TYPE_PLACEHOLDER = 0
private val ITEM_VIEW_TYPE_RATE = 1

class CurrencyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //var responseList: List<Rate>? = null
    var listWithHeader: List<DataItem>? = null

    class PlaceholderViewHolder(itemView: View, val binding: PlaceholderItemViewBinding) :
        RecyclerView.ViewHolder(itemView) {
        companion object {
            fun inflateFrom(parent: ViewGroup): PlaceholderViewHolder {
                val itemView =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.placeholder_item_view, parent, false)
                val binding = PlaceholderItemViewBinding.bind(itemView)
                return PlaceholderViewHolder(itemView, binding)
            }
        }
    }

    class CurrencyViewHolder(itemView: View, val binding: CurrencyItemViewBinding) :
        RecyclerView.ViewHolder(itemView) {
        companion object {
            fun inflateFrom(parent: ViewGroup): CurrencyViewHolder {
                val itemView =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.currency_item_view, parent, false)
                val binding = CurrencyItemViewBinding.bind(itemView)
                return CurrencyViewHolder(itemView, binding)
            }
        }
    }

    fun addPlaceHolderAndSubmitList (responseList: List<Rate>) {
        listWithHeader = listOf(DataItem.PlaceHolder) + responseList.map { DataItem.RateItem(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_PLACEHOLDER -> PlaceholderViewHolder.inflateFrom(parent)
            ITEM_VIEW_TYPE_RATE -> CurrencyViewHolder.inflateFrom(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CurrencyViewHolder -> {
                holder.binding.apply {
                    tvCurrencyEng.text =
                        (listWithHeader?.get(position) as DataItem.RateItem).rate.iso
                    tvCurrencyRus.text =
                        (listWithHeader?.get(position) as DataItem.RateItem).rate.name
                    tvCurrencyToday.text =
                        (listWithHeader?.get(position) as DataItem.RateItem).rate.rate?.format(4)
                    tvQuantity.text =
                        (listWithHeader?.get(position) as DataItem.RateItem).rate.quantity.toString()
                    tvCurrencyTomorrow.text =
                        (listWithHeader?.get(position) as DataItem.RateItem).rate.rateTomorrow?.format(4)
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return listWithHeader?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return when (listWithHeader?.get(position)) {
            is DataItem.PlaceHolder -> ITEM_VIEW_TYPE_PLACEHOLDER
            is DataItem.RateItem -> ITEM_VIEW_TYPE_RATE
            null -> 10
        }
    }

    sealed class DataItem {
        data class RateItem(val rate: Rate) : DataItem()
        object PlaceHolder : DataItem()
    }
}