package com.christianrincon.taxit.ui.calculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.christianrincon.taxit.R
import com.christianrincon.taxit.adapter.LineItemAdapter

class CalculatorFragment : Fragment() {

    private lateinit var lineItemAdapter: LineItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calculator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        setupZipToggle(view)
        setupButtons(view)
        setupZipLookup(view)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_line_items)
        lineItemAdapter = LineItemAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = lineItemAdapter

        // Start with one blank row
        lineItemAdapter.addItem()

        // Add Item button appends a new blank row
        view.findViewById<TextView>(R.id.btn_add_item).setOnClickListener {
            lineItemAdapter.addItem()
        }
    }

    private fun setupZipToggle(view: View) {
        val zipExpanded = view.findViewById<View>(R.id.layout_zip_expanded)
        val zipCollapsed = view.findViewById<View>(R.id.layout_zip_collapsed)
        val btnEdit = view.findViewById<TextView>(R.id.btn_edit_zip)

        // Edit tapped — go back to State A
        btnEdit.setOnClickListener {
            zipCollapsed.visibility = View.GONE
            zipExpanded.visibility = View.VISIBLE
        }
    }

    private fun setupButtons(view: View) {
        val zipExpanded = view.findViewById<View>(R.id.layout_zip_expanded)
        val zipCollapsed = view.findViewById<View>(R.id.layout_zip_collapsed)

        // Calculate Tax — collapse ZIP to State B
        view.findViewById<View>(R.id.btn_calculate).setOnClickListener {
            val zip = view.findViewById<EditText>(R.id.et_zip).text.toString().trim()
            if (zip.length < 5) {
                view.findViewById<EditText>(R.id.et_zip).error = "Please enter a valid 5-digit ZIP code"
                return@setOnClickListener
            }
            zipExpanded.visibility = View.GONE
            zipCollapsed.visibility = View.VISIBLE
            view.findViewById<NestedScrollView>(R.id.scroll_content).smoothScrollTo(0, 0)
        }

        // Clear All — reset everything back to State A
        view.findViewById<View>(R.id.btn_clear).setOnClickListener {
            zipExpanded.visibility = View.VISIBLE
            zipCollapsed.visibility = View.GONE
            lineItemAdapter.clearItems()
            lineItemAdapter.addItem()
        }
    }

    private fun setupZipLookup(view: View) {
        val etZip = view.findViewById<EditText>(R.id.et_zip)
        val tvStatus = view.findViewById<TextView>(R.id.tv_zip_status)
        val cardRateBreakdown = view.findViewById<View>(R.id.card_rate_breakdown)
        val tvStateRate = view.findViewById<TextView>(R.id.tv_state_rate)
        val tvCountyRate = view.findViewById<TextView>(R.id.tv_county_rate)
        val tvCityRate = view.findViewById<TextView>(R.id.tv_city_rate)
        val tvCombinedRate = view.findViewById<TextView>(R.id.tv_combined_rate)

        etZip.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val zip = s.toString().trim()
                if (zip.length == 5) {
                    // Show loading status
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = getString(R.string.status_loading)
                    cardRateBreakdown.visibility = View.GONE

                    // Simulate a short delay then show mock rates
                    etZip.postDelayed({
                        tvStatus.text = getString(R.string.status_success)
                        tvStateRate.text = "6.000%"
                        tvCountyRate.text = "0.250%"
                        tvCityRate.text = "0.000%"
                        tvCombinedRate.text = "6.250%"
                        cardRateBreakdown.visibility = View.VISIBLE
                    }, 800)
                } else {
                    tvStatus.visibility = View.GONE
                    cardRateBreakdown.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

}