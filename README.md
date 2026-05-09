# TaxIt — Location-Aware Sales Tax Calculator
**Phase:** Phase 2 — UI Prototype

---

## Screens

### Calculator Screen
The main screen of the app. It has two states:
- **State A (ZIP Expanded):** The user enters a 5-digit ZIP code. A rate breakdown card shows state, county, city, and combined tax rates.
- **State B (ZIP Collapsed):** After confirming a ZIP, the lookup section collapses into a compact summary bar showing the city, state, and combined rate. The user can tap Edit to go back to State A.

The screen also includes:
- A dark display panel  at the top showing Subtotal, Tax, and Total in real time
- A dynamic line items list where users enter a description, quantity, and price per item
- An Add Item button to add new rows
- Calculate Tax and Clear All buttons at the bottom

### History Screen
Displays list of past tax calculations. Each one is shown as a card with the ZIP code, city/state label, pre-tax amount, tax applied, and total. An empty state message is shown when no calculations exist yet.

---

## Navigation
The app uses a single-activity architecture with a bottom navigation bar that is always visible. Tapping Calculator or History switches between the two fragments using the Navigation Component.

---

## Design Choices
- **Color palette:** Dark panel (#0D0D0D / Black) for the top display area, white content area below, red accent (#E53935) for interactive elements, green (#4CAF50) for totals and the ZIP status dot while the zip api will be loading
- **Typography:** Material Design text sizes — 22sp for titles, 18sp for values, 13sp for labels
- **Layout:** ConstraintLayout as the root for responsiveness, RecyclerView for dynamic lists
- **Theme:** MaterialComponents DayNight with no Dark Mode so far

---

## Resource Usage
- `colors.xml` — all color values defined in one place
- `strings.xml` — all user-facing text defined for localization readiness
- `themes.xml` — app theme and reusable styles (TaxIt.DarkPanel, TaxIt.Label, TaxIt.Value, TaxIt.TotalValue)


