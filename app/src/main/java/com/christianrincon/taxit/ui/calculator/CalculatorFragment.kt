package com.christianrincon.taxit.ui.calculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.christianrincon.taxit.R
import com.christianrincon.taxit.adapter.LineItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalculatorFragment : Fragment() {

    // The ViewModel keeps calculator data alive when the screen is recreated.
    private val viewModel: CalculatorViewModel by viewModels()
    private lateinit var lineItemAdapter: LineItemAdapter
    private var discountTextWatcher: TextWatcher? = null
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

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
        setupDiscountInput(view)
        observeViewModel(view)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_line_items)
        lineItemAdapter = LineItemAdapter(
            onItemChanged = { index, item ->
                // Row edits flow from the adapter into the ViewModel.
                viewModel.updateLineItem(index, item)
            },
            onItemDeleted = { index ->
                viewModel.removeLineItem(index)
                // Keep one blank row visible so the calculator never looks empty.
                if (viewModel.lineItems.value.isEmpty()) {
                    viewModel.addLineItem()
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = lineItemAdapter

        view.findViewById<TextView>(R.id.btn_add_item).setOnClickListener {
            viewModel.addLineItem()
        }
    }

    private fun setupZipToggle(view: View) {
        val zipExpanded = view.findViewById<View>(R.id.layout_zip_expanded)
        val zipCollapsed = view.findViewById<View>(R.id.layout_zip_collapsed)
        val btnEdit = view.findViewById<TextView>(R.id.btn_edit_zip)

        // Lets the user reopen the ZIP lookup section after calculating.
        btnEdit.setOnClickListener {
            zipCollapsed.visibility = View.GONE
            zipExpanded.visibility = View.VISIBLE
        }
    }

    private fun setupButtons(view: View) {
        val zipExpanded = view.findViewById<View>(R.id.layout_zip_expanded)
        val zipCollapsed = view.findViewById<View>(R.id.layout_zip_collapsed)

        view.findViewById<View>(R.id.btn_calculate).setOnClickListener {
            val zip = view.findViewById<EditText>(R.id.et_zip).text.toString().trim()
            // Do not calculate until the ZIP format is valid.
            if (zip.length < 5) {
                view.findViewById<EditText>(R.id.et_zip).error = "Please enter a valid 5-digit ZIP code"
                return@setOnClickListener
            }

            // If this exact ZIP has not loaded yet, start lookup first.
            if (!viewModel.hasSuccessfulRateForZip(zip)) {
                viewModel.lookUpZipRate(zip)
                return@setOnClickListener
            }

            // Saves to History and formats prices after the calculation is complete.
            viewModel.saveCompletedCalculation(zip)
            lineItemAdapter.formatPricesAsCurrency()
            collapseZipSection(view)
        }

        view.findViewById<View>(R.id.btn_clear).setOnClickListener {
            zipExpanded.visibility = View.VISIBLE
            zipCollapsed.visibility = View.GONE
            view.findViewById<EditText>(R.id.et_zip).text.clear()
            view.findViewById<EditText>(R.id.et_discount_percent).text.clear()
            viewModel.clearZipLookup()
            viewModel.clearLineItems()
            viewModel.clearDiscount()
            updateTotals(view)
        }
    }

    private fun setupZipLookup(view: View) {
        val etZip = view.findViewById<EditText>(R.id.et_zip)

        etZip.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val zip = s.toString().trim()
                // A 5-digit ZIP automatically triggers the tax lookup.
                if (zip.length == 5) {
                    viewModel.lookUpZipRate(zip)
                } else {
                    viewModel.clearZipLookup()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupDiscountInput(view: View) {
        val etDiscountPercent = view.findViewById<EditText>(R.id.et_discount_percent)

        discountTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Blank or invalid discount input is treated as 0%.
                val rawText = s.toString()
                val parsedDiscount = rawText.toDoubleOrNull()
                val rawDiscount = parsedDiscount ?: 0.0
                viewModel.updateDiscountPercent(rawDiscount)

                val clampedDiscount = rawDiscount.coerceIn(MIN_DISCOUNT_PERCENT, MAX_DISCOUNT_PERCENT)
                if ((parsedDiscount == null && rawText.isNotBlank()) || rawDiscount != clampedDiscount) {
                    updateDiscountInputText(etDiscountPercent, clampedDiscount)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        discountTextWatcher?.let { etDiscountPercent.addTextChangedListener(it) }
    }

    private fun observeViewModel(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Collect StateFlows only while the Fragment is visible.
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.zipLookupState.collect { state ->
                        renderZipLookupState(view, state)
                        updateTotals(view)
                    }
                }

                launch {
                    viewModel.lineItems.collect { items ->
                        lineItemAdapter.submitItems(items)
                        updateTotals(view)
                    }
                }

                launch {
                    viewModel.discountPercent.collect { discountPercent ->
                        updateDiscountInputText(
                            view.findViewById<EditText>(R.id.et_discount_percent),
                            discountPercent
                        )
                        updateTotals(view)
                    }
                }
            }
        }
    }

    private fun renderZipLookupState(
        view: View,
        state: CalculatorViewModel.ZipLookupState
    ) {
        val tvStatus = view.findViewById<TextView>(R.id.tv_zip_status)
        val cardRateBreakdown = view.findViewById<View>(R.id.card_rate_breakdown)
        val tvStateRate = view.findViewById<TextView>(R.id.tv_state_rate)
        val tvCountyRate = view.findViewById<TextView>(R.id.tv_county_rate)
        val tvCityRate = view.findViewById<TextView>(R.id.tv_city_rate)
        val tvCombinedRate = view.findViewById<TextView>(R.id.tv_combined_rate)
        val tvZipSummary = view.findViewById<TextView>(R.id.tv_zip_summary)
        val btnCalculate = view.findViewById<Button>(R.id.btn_calculate)

        // Shows the correct ZIP lookup UI for idle, loading, success, or error.
        when (state) {
            CalculatorViewModel.ZipLookupState.Idle -> {
                btnCalculate.isEnabled = true
                tvStatus.visibility = View.GONE
                cardRateBreakdown.visibility = View.GONE
            }

            is CalculatorViewModel.ZipLookupState.Loading -> {
                btnCalculate.isEnabled = false
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = getString(R.string.status_loading)
                cardRateBreakdown.visibility = View.GONE
            }

            is CalculatorViewModel.ZipLookupState.Success -> {
                val rate = state.rate
                btnCalculate.isEnabled = true
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = getString(R.string.status_success)
                tvStateRate.text = formatRateOrPremium(rate.stateRate)
                tvCountyRate.text = formatRateOrPremium(rate.countyRate)
                tvCityRate.text = formatRateOrPremium(rate.cityRate)
                tvCombinedRate.text = formatCombinedRate(rate.combinedRate, rate.stateRate)
                tvZipSummary.text = formatZipSummary(
                    zip = rate.zipCode,
                    cityName = rate.cityName,
                    stateName = rate.stateName,
                    combinedRate = formatCombinedRate(rate.combinedRate, rate.stateRate)
                )
                cardRateBreakdown.visibility = View.VISIBLE
            }

            is CalculatorViewModel.ZipLookupState.Error -> {
                btnCalculate.isEnabled = true
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = state.message
                cardRateBreakdown.visibility = View.GONE
            }
        }
    }

    private fun updateTotals(view: View) {
        // Reads calculated values from the ViewModel and displays them as money.
        view.findViewById<TextView>(R.id.tv_subtotal).text = currencyFormatter.format(viewModel.subtotal)
        view.findViewById<TextView>(R.id.tv_discount).text = currencyFormatter.format(viewModel.discountAmount)
        view.findViewById<TextView>(R.id.tv_tax).text = currencyFormatter.format(viewModel.taxAmount)
        view.findViewById<TextView>(R.id.tv_total).text = currencyFormatter.format(viewModel.total)
    }

    private fun updateDiscountInputText(
        etDiscountPercent: EditText,
        discountPercent: Double
    ) {
        val currentText = etDiscountPercent.text.toString()
        if (currentText.isBlank()) return

        val displayText = formatDiscountPercentForInput(discountPercent)
        if (currentText == displayText) return

        discountTextWatcher?.let { etDiscountPercent.removeTextChangedListener(it) }
        etDiscountPercent.setText(displayText)
        etDiscountPercent.setSelection(displayText.length)
        discountTextWatcher?.let { etDiscountPercent.addTextChangedListener(it) }
    }

    private fun formatDiscountPercentForInput(discountPercent: Double): String {
        return if (discountPercent % 1.0 == 0.0) {
            discountPercent.toInt().toString()
        } else {
            discountPercent.toString()
        }
    }

    private fun formatRateOrPremium(rate: String): String {
        val decimalRate = rate.toDoubleOrNull() ?: return PREMIUM_FEATURE_LABEL
        return formatDecimalRateAsPercent(decimalRate)
    }

    private fun formatCombinedRate(combinedRate: String, fallbackStateRate: String): String {
        val combinedDecimal = combinedRate.toDoubleOrNull()
        if (combinedDecimal != null && combinedDecimal > 0.0) {
            return formatDecimalRateAsPercent(combinedDecimal)
        }

        val stateDecimal = fallbackStateRate.toDoubleOrNull()
        return if (stateDecimal != null && stateDecimal > 0.0) {
            formatDecimalRateAsPercent(stateDecimal)
        } else {
            PREMIUM_FEATURE_LABEL
        }
    }

    private fun formatDecimalRateAsPercent(decimalRate: Double): String {
        // Formats a decimal tax rate like 0.08875 into 8.88%.
        return "%.2f%%".format(Locale.US, decimalRate * 100.0)
    }

    private fun formatZipSummary(
        zip: String,
        cityName: String,
        stateName: String,
        combinedRate: String
    ): String {
        return if (cityName.isNotBlank() && stateName.isNotBlank()) {
            "$cityName, $stateName — $combinedRate"
        } else {
            "ZIP: $zip — $combinedRate"
        }
    }

    private fun collapseZipSection(view: View) {
        // Moves the ZIP lookup into the compact summary after a successful calculation.
        view.findViewById<View>(R.id.layout_zip_expanded).visibility = View.GONE
        view.findViewById<View>(R.id.layout_zip_collapsed).visibility = View.VISIBLE
        view.findViewById<NestedScrollView>(R.id.scroll_content).smoothScrollTo(0, 0)
    }

    private companion object {
        const val PREMIUM_FEATURE_LABEL = "Premium feature"
        const val MIN_DISCOUNT_PERCENT = 0.0
        const val MAX_DISCOUNT_PERCENT = 100.0
    }

}
