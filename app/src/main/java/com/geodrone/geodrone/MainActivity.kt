package com.geodrone.geodrone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.geodrone.geodrone.api.ApiClient
import com.geodrone.geodrone.api.SessionManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.initialize(applicationContext)
        val destination = if (SessionManager.token(this).isNullOrBlank()) {
            LoginActivity::class.java
        } else {
            HomeActivity::class.java
        }
        val intent = Intent(this, destination)
        startActivity(intent)
        finish()
    }
}
