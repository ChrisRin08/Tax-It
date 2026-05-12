package com.christianrincon.taxit.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.christianrincon.taxit.R
import com.christianrincon.taxit.adapter.HistoryAdapter
import com.christianrincon.taxit.data.db.TaxCalculationEntity
import com.christianrincon.taxit.model.HistoryEntry
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        observeHistory(view)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_history)

        historyAdapter = HistoryAdapter(
            onDeleteClicked = { id ->
                viewModel.deleteCalculation(id)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = historyAdapter

        view.findViewById<TextView>(R.id.btn_clear_history).setOnClickListener {
            showClearHistoryConfirmationDialog()
        }
    }

    private fun showClearHistoryConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_clear_history_title)
            .setMessage(R.string.dialog_clear_history_message)
            .setPositiveButton(R.string.dialog_clear_history_confirm) { _, _ ->
                viewModel.clearHistory()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun observeHistory(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_history)
        val emptyState = view.findViewById<View>(R.id.tv_empty_history)
        val btnClearHistory = view.findViewById<TextView>(R.id.btn_clear_history)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.calculationHistory.collect { calculations ->
                    historyAdapter.submitList(calculations.map { it.toHistoryEntry() })

                    if (calculations.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        btnClearHistory.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        btnClearHistory.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun TaxCalculationEntity.toHistoryEntry(): HistoryEntry {
        return HistoryEntry(
            id = id,
            zip = zipCode,
            cityState = listOf(cityName, stateName)
                .filter { it.isNotBlank() }
                .joinToString(", "),
            subtotal = subtotal,
            tax = taxAmount,
            total = total,
            timestamp = timestamp
        )
    }
}
