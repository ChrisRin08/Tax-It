package com.christianrincon.taxit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.christianrincon.taxit.R
import com.christianrincon.taxit.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onDeleteClicked: (id: Long) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    // List of past calculations to display
    private val entries = mutableListOf<HistoryEntry>()
    private val dateFormatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_card, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size

    // Replaces the entire list with new data
    fun submitList(newEntries: List<HistoryEntry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvZip = itemView.findViewById<TextView>(R.id.tv_history_zip)
        private val tvDate = itemView.findViewById<TextView>(R.id.tv_history_date)
        private val tvAmount = itemView.findViewById<TextView>(R.id.tv_history_amount)
        private val tvTax = itemView.findViewById<TextView>(R.id.tv_history_tax)
        private val tvTotal = itemView.findViewById<TextView>(R.id.tv_history_total)
        private val btnDelete = itemView.findViewById<TextView>(R.id.btn_delete_history)

        fun bind(entry: HistoryEntry) {
            // Show ZIP and city together
            tvZip.text = if (entry.cityState.isBlank()) {
                "ZIP: ${entry.zip}"
            } else {
                "ZIP: ${entry.zip} — ${entry.cityState}"
            }
            tvDate.text = dateFormatter.format(Date(entry.timestamp))

            // Format amounts as dollar values
            tvAmount.text = "$%.2f".format(entry.subtotal)
            tvTax.text = "$%.2f".format(entry.tax)
            tvTotal.text = "$%.2f".format(entry.total)

            btnDelete.setOnClickListener {
                onDeleteClicked(entry.id)
            }
        }
    }
}
