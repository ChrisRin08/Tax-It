package com.christianrincon.taxit.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christianrincon.taxit.data.TaxRepository
import com.christianrincon.taxit.data.db.CachedZipRateEntity
import com.christianrincon.taxit.model.LineItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val taxRepository: TaxRepository
) : ViewModel() {

    // Exposes ZIP lookup progress and results to the Fragment.
    private val _zipLookupState = MutableStateFlow<ZipLookupState>(ZipLookupState.Idle)
    val zipLookupState: StateFlow<ZipLookupState> = _zipLookupState.asStateFlow()

    // The ViewModel is the source of truth for calculator rows.
    private val _lineItems = MutableStateFlow(listOf(LineItem()))
    val lineItems: StateFlow<List<LineItem>> = _lineItems.asStateFlow()

    // Whole-order discount percent entered on the Calculator screen.
    private val _discountPercent = MutableStateFlow(0.0)
    val discountPercent: StateFlow<Double> = _discountPercent.asStateFlow()

    val subtotal: Double
        get() = _lineItems.value.sumOf { it.lineTotal() }

    val discountAmount: Double
        get() = subtotal * (_discountPercent.value / 100.0)

    private val taxableAmount: Double
        get() = (subtotal - discountAmount).coerceAtLeast(0.0)

    val taxAmount: Double
        get() = taxableAmount * currentTaxRate()

    val total: Double
        get() = taxableAmount + taxAmount

    fun lookUpZipRate(zip: String) {
        val trimmedZip = zip.trim()
        // Stop early if the user has not entered a full ZIP code.
        if (trimmedZip.length != ZIP_CODE_LENGTH) {
            _zipLookupState.value = ZipLookupState.Error(
                zip = trimmedZip,
                message = "Please enter a valid 5-digit ZIP code"
            )
            return
        }

        viewModelScope.launch {
            // Loading disables Calculate until the lookup finishes.
            _zipLookupState.value = ZipLookupState.Loading(trimmedZip)
            _zipLookupState.value = try {
                ZipLookupState.Success(
                    zip = trimmedZip,
                    rate = taxRepository.getTaxRate(trimmedZip)
                )
            } catch (e: Exception) {
                ZipLookupState.Error(
                    zip = trimmedZip,
                    message = "Couldn't fetch the tax rate. Check your connection and try again."
                )
            }
        }
    }

    fun hasSuccessfulRateForZip(zip: String): Boolean {
        // Prevents calculating with an old rate after the ZIP field changes.
        val successState = _zipLookupState.value as? ZipLookupState.Success ?: return false
        return successState.zip == zip.trim()
    }

    fun addLineItem() {
        _lineItems.update { items -> items + LineItem() }
    }

    fun removeLineItem(index: Int) {
        _lineItems.update { items ->
            items.filterIndexed { itemIndex, _ -> itemIndex != index }
        }
    }

    fun updateLineItem(index: Int, lineItem: LineItem) {
        _lineItems.update { items ->
            items.mapIndexed { itemIndex, existingItem ->
                if (itemIndex == index) lineItem.copy() else existingItem
            }
        }
    }

    fun updateDiscountPercent(value: Double) {
        _discountPercent.value = value.coerceIn(MIN_DISCOUNT_PERCENT, MAX_DISCOUNT_PERCENT)
    }

    fun clearLineItems() {
        _lineItems.value = listOf(LineItem())
    }

    fun clearDiscount() {
        _discountPercent.value = MIN_DISCOUNT_PERCENT
    }

    fun clearZipLookup() {
        _zipLookupState.value = ZipLookupState.Idle
    }

    fun saveCompletedCalculation(zip: String) {
        // Only save when the current ZIP matches the successful lookup.
        if (!hasSuccessfulRateForZip(zip)) return

        val successState = _zipLookupState.value as? ZipLookupState.Success ?: return
        val rate = successState.rate

        viewModelScope.launch {
            taxRepository.saveCalculation(
                zip = rate.zipCode,
                cityName = rate.cityName,
                stateName = rate.stateName,
                combinedRate = rate.combinedRate,
                subtotal = subtotal,
                tax = taxAmount,
                total = total
            )
        }
    }

    private fun currentTaxRate(): Double {
        // Uses the combined rate from the successful lookup for tax math.
        val successState = _zipLookupState.value as? ZipLookupState.Success ?: return 0.0
        val rawRate = successState.rate.combinedRate.trim().removeSuffix("%")
        val parsedRate = rawRate.toDoubleOrNull() ?: return 0.0

        return if (parsedRate > 1.0) {
            parsedRate / 100.0
        } else {
            parsedRate
        }
    }

    // Describes the ZIP lookup state in a simple way for the UI.
    sealed interface ZipLookupState {
        data object Idle : ZipLookupState
        data class Loading(val zip: String) : ZipLookupState
        data class Success(val zip: String, val rate: CachedZipRateEntity) : ZipLookupState
        data class Error(val zip: String, val message: String) : ZipLookupState
    }

    private companion object {
        const val ZIP_CODE_LENGTH = 5
        const val MIN_DISCOUNT_PERCENT = 0.0
        const val MAX_DISCOUNT_PERCENT = 100.0
    }
}
