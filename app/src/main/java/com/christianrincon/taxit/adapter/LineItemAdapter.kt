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

class LineItemAdapter : RecyclerView.Adapter<LineItemAdapter.LineItemViewHolder>() {

    // The list of items the RecyclerView will display
    private val items = mutableListOf<LineItem>()

    // Called when the RecyclerView needs a new row view
    // It inflates item_line_item.xml and wraps it in a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_line_item, parent, false)
        return LineItemViewHolder(view)
    }

    // Called to fill each row with its data
    override fun onBindViewHolder(holder: LineItemViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    // Tells the RecyclerView how many rows to show
    override fun getItemCount(): Int = items.size

    // Adds a blank new row to the list
    fun addItem() {
        items.add(LineItem())
        notifyItemInserted(items.size - 1)
    }

    // Deletes a row at a specific position
    fun removeItem(position: Int) {
        if (position < items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Clears all rows
    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    // ViewHolder holds references to the views inside each row
    // so we don't have to find them every time the row scrolls
    inner class LineItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val etDescription = itemView.findViewById<EditText>(R.id.et_item_description)
        private val etQuantity = itemView.findViewById<EditText>(R.id.et_item_quantity)
        private val etPrice = itemView.findViewById<EditText>(R.id.et_item_price)
        private val btnDelete = itemView.findViewById<TextView>(R.id.btn_delete_item)

        // Watchers defined as properties so we can remove them before rebinding
        private val descriptionWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items[pos].description = s.toString()
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
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        private val priceWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items[pos].price = s.toString().toDoubleOrNull() ?: 0.0
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        fun bind(item: LineItem, position: Int) {

            // Remove old listeners before setting text
            etDescription.removeTextChangedListener(descriptionWatcher)
            etQuantity.removeTextChangedListener(quantityWatcher)
            etPrice.removeTextChangedListener(priceWatcher)

            // Fill fields with existing data
            etDescription.setText(item.description)
            etQuantity.setText(if (item.quantity > 1) item.quantity.toString() else "")
            etPrice.setText(if (item.price > 0) item.price.toString() else "")

            // Reattach listeners
            etDescription.addTextChangedListener(descriptionWatcher)
            etQuantity.addTextChangedListener(quantityWatcher)
            etPrice.addTextChangedListener(priceWatcher)

            // Delete button — safe position check
            btnDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    removeItem(pos)
                }
            }
        }
    }
}