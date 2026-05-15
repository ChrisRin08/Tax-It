package com.christianrincon.taxit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Starts Hilt so dependencies can be injected throughout the app.
@HiltAndroidApp
class TaxItApplication : Application()
