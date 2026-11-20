package com.geodrone.geodrone

import android.os.Bundle
import com.geodrone.geodrone.api.ApiActivity
import com.geodrone.geodrone.api.ApiClient
import com.geodrone.geodrone.databinding.ActivityProfileBinding

class ProfileActivity : ApiActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarProfile.setNavigationOnClickListener {
            finish()
        }

        binding.switchDarkMode.isChecked = ThemeManager.isDarkMode(this)
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.setDarkMode(this, isChecked)
        }

        executeApi(
            request = { ApiClient.service.currentUser() },
            onSuccess = { user ->
                binding.textUserName.text = user.name
                binding.textUserEmail.text = user.email
                binding.textUserRole.text = user.role
            },
            onError = {
                binding.textUserName.text = "Usuario GeoDrone"
                binding.textUserEmail.text = "usuario@geodrone.com"
            }
        )

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}
