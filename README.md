# TaxIt

**Final Phase 3 / Course Final Submission**  
Kotlin Android sales tax calculator built as the completed Phase 3 version of TaxIt.

## Overview

TaxIt helps users estimate sales tax by ZIP code. The app looks up the location and tax breakdown for a ZIP code, lets the user enter line items, calculates subtotal, tax, and total, and saves completed calculations to local history.

The final Phase 3 version connects the UI to real ViewModels, a repository layer, network APIs, and a Room database.

## Main Features

- ZIP-based sales tax lookup
- City/state display from the tax lookup response
- Full tax breakdown: state, county, city, district/special, and combined rate
- Dynamic line item entry with description, quantity, and price
- Subtotal, tax, and total calculation
- Price fields format as currency after calculation
- Saved calculation history
- History cards show date/time
- Delete one saved history entry with the X button
- Clear all history with a confirmation dialog
- Local ZIP tax-rate cache
- API keys stored in `local.properties`, not committed to GitHub

## Tech Stack

- Kotlin
- Android Jetpack Fragments
- MVVM architecture
- ViewModel with StateFlow
- Hilt dependency injection
- Retrofit and OkHttp
- Room database
- RecyclerView adapters
- Material Components

## Architecture

TaxIt uses a simple MVVM structure:

- **Fragments** control screen behavior and render UI state.
- **ViewModels** hold screen state and business logic using `StateFlow`.
- **Repository** coordinates network calls, caching, and saved history.
- **Retrofit services** handle remote API calls.
- **Room DAOs/entities** persist ZIP tax cache data and saved calculations.

This keeps calculator and history logic out of the Fragment layer while still keeping the app small and understandable for the final project.

## APIs Used

TaxIt uses the **Ziptax API** for sales tax and location data.

Useful fields from the Ziptax response include:

- `geoPostalCode`
- `geoCity`
- `geoCounty`
- `geoState`
- `stateSalesTax`
- `countySalesTax`
- `citySalesTax`
- `districtSalesTax`
- `taxSales`

The app maps these values into its local cache and uses `taxSales` as the combined sales tax rate.

## Local Setup

Create or update `local.properties` in the project root and add:

```properties
ZIPTAX_API_KEY=your_ziptax_api_key_here
```

Do not commit `local.properties` to GitHub. It contains local machine settings and API keys.

## Running the Project

1. Open the project in Android Studio.
2. Confirm `local.properties` contains `ZIPTAX_API_KEY`.
3. Sync Gradle.
4. Run the app on an emulator or Android device.

You can also build from the command line:

```bash
./gradlew assembleDebug
```

## Demo Walkthrough

1. Open TaxIt on the Calculator screen.
2. Enter a ZIP code such as `10001`.
3. Wait for the tax rate lookup to complete.
4. Review the city/state and tax breakdown card.
5. Add one or more line items with quantity and price.
6. Tap **Calculate Tax**.
7. Confirm subtotal, tax, total, and formatted price fields.
8. Open the History screen.
9. Review the saved calculation card with date/time.
10. Delete one entry with the X button.
11. Use **Clear All** and confirm the dialog to delete all history.

## Known Limitations

- ZIP-level sales tax lookup is an estimate compared to exact street-address tax calculation.
- The API key is exposed through `BuildConfig`, which is acceptable for a class demo but not production secure.
- The app requires network access for fresh ZIP lookups unless a valid cached rate already exists.

## Future Improvements

- Add a backend proxy so API keys are never shipped in the app.
- Support exact street-address tax lookup.
- Add export/share options for saved history.
- Improve visual polish and accessibility details.
