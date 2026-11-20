package com.geodrone.geodrone

import android.app.Application
import com.geodrone.geodrone.api.ApiClient

class GeodroneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.applySavedTheme(this)
        ApiClient.initialize(this)
    }
}
