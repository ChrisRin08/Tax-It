package com.christianrincon.taxit.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.christianrincon.taxit.R
import com.christianrincon.taxit.adapter.HistoryAdapter
import com.christianrincon.taxit.model.HistoryEntry

class HistoryFragment : Fragment() {

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
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_history)
        val emptyState = view.findViewById<View>(R.id.tv_empty_history)

        historyAdapter = HistoryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = historyAdapter

        // Mock data — simulates two past calculations
        val mockHistory = listOf(
            HistoryEntry(
                zip = "90001",
                cityState = "Los Angeles, CA",
                subtotal = 500.00,
                tax = 31.25,
                total = 531.25
            ),
            HistoryEntry(
                zip = "10001",
                cityState = "New York, NY",
                subtotal = 200.00,
                tax = 17.75,
                total = 217.75
            )
        )

        historyAdapter.submitList(mockHistory)

        // Show empty state only if list is empty
        if (mockHistory.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}