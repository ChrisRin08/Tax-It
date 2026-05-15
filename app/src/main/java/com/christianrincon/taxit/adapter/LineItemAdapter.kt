package com.christianrincon.taxit.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.christianrincon.taxit.R
import com.christianrincon.taxit.model.LineItem
import java.text.NumberFormat
import java.util.Locale

class LineItemAdapter(
    private val onItemChanged: (position: Int, item: LineItem) -> Unit,
    private val onItemDeleted: (position: Int) -> Unit
) : RecyclerView.Adapter<LineItemAdapter.LineItemViewHolder>() {

    // Local row copies used for binding RecyclerView fields.
    private val items = mutableListOf<LineItem>()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    private var shouldFormatPricesAsCurrency = false

    // Creates a new row view when RecyclerView needs one.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_line_item, parent, false)
        return LineItemViewHolder(view)
    }

    // Fills each visible row with the matching line item.
    override fun onBindViewHolder(holder: LineItemViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    // Adds a blank row when the user taps Add Item.
    fun addItem() {
        shouldFormatPricesAsCurrency = false
        items.add(LineItem())
        notifyItemInserted(items.size - 1)
    }

    // Removes a row from the adapter list.
    fun removeItem(position: Int) {
        if (position < items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Clears rows when the calculator is reset.
    fun clearItems() {
        shouldFormatPricesAsCurrency = false
        items.clear()
        notifyDataSetChanged()
    }

    fun submitItems(newItems: List<LineItem>) {
        // Refreshes adapter rows from the ViewModel source of truth.
        if (items == newItems) return

        shouldFormatPricesAsCurrency = false
        items.clear()
        items.addAll(newItems.map { it.copy() })
        notifyDataSetChanged()
    }

    fun formatPricesAsCurrency() {
        // Rebinds rows so prices display like $100.00 after calculation.
        shouldFormatPricesAsCurrency = true
        notifyDataSetChanged()
    }

    // Holds the views for one line item row.
    inner class LineItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val etDescription = itemView.findViewById<EditText>(R.id.et_item_description)
        private val etQuantity = itemView.findViewById<EditText>(R.id.et_item_quantity)
        private val etPrice = itemView.findViewById<EditText>(R.id.et_item_price)
        private val btnDelete = itemView.findViewById<TextView>(R.id.btn_delete_item)

        // Watchers notify the Fragment whenever the user edits a row.
        private val descriptionWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items[pos].description = s.toString()
                    onItemChanged(pos, items[pos].copy())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        private val quantityWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items[pos].quantity = s.toString().toIntOrNull() ?: 1
                    onItemChanged(pos, items[pos].copy())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        private val priceWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    shouldFormatPricesAsCurrency = false
                    items[pos].price = s.toString().toPriceDoubleOrNull() ?: 0.0
                    onItemChanged(pos, items[pos].copy())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        fun bind(item: LineItem, position: Int) {

            // Remove listeners before setText so recycled rows do not fire duplicate updates.
            etDescription.removeTextChangedListener(descriptionWatcher)
            etQuantity.removeTextChangedListener(quantityWatcher)
            etPrice.removeTextChangedListener(priceWatcher)

            // Show the current row values from the adapter list.
            etDescription.setText(item.description)
            etQuantity.setText(item.quantity.toString())
            etPrice.setText(formatPriceForDisplay(item.price))

            // Reattach listeners after binding is complete.
            etDescription.addTextChangedListener(descriptionWatcher)
            etQuantity.addTextChangedListener(quantityWatcher)
            etPrice.addTextChangedListener(priceWatcher)

            // Use the current adapter position because RecyclerView rows can move.
            btnDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemDeleted(pos)
                }
            }
        }
    }

    private fun String.toPriceDoubleOrNull(): Double? {
        // Allows prices typed as $100.00 or 1,000.00 to still calculate.
        val normalized = replace("$", "")
            .replace(",", "")
            .trim()
        return normalized.toDoubleOrNull()
    }

    private fun formatPriceForDisplay(price: Double): String {
        // Keep blank rows visually empty until there is a real price.
        if (price <= 0.0) return ""
        return if (shouldFormatPricesAsCurrency) {
            currencyFormatter.format(price)
        } else {
            price.toString()
        }
    }
}
