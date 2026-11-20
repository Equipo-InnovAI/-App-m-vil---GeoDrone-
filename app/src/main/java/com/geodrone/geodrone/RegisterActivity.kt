package com.geodrone.geodrone

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.geodrone.geodrone.api.ApiActivity
import com.geodrone.geodrone.api.ApiClient
import com.geodrone.geodrone.api.RegisterRequest
import com.geodrone.geodrone.api.SessionManager
import com.geodrone.geodrone.databinding.ActivityRegisterBinding
import com.geodrone.geodrone.db.LocalAuthDatabase

class RegisterActivity : ApiActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener {
            if (validateInputs()) {
                val name = binding.inputName.text?.toString()?.trim().orEmpty()
                val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
                val password = binding.inputPassword.text?.toString().orEmpty()

                val localDb = LocalAuthDatabase(this)

                executeApi(
                    request = { ApiClient.service.register(RegisterRequest(name, email, password)) },
                    onSuccess = { response ->
                        localDb.registerUser(name, email, password)
                        val token = response.authToken
                        if (!token.isNullOrBlank()) {
                            SessionManager.saveToken(this, token)
                            Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Registro exitoso. Inicia sesión para continuar.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    },
                    onError = { error ->
                        val regResult = localDb.registerUser(name, email, password)
                        when (regResult) {
                            LocalAuthDatabase.RegisterResult.USER_ALREADY_EXISTS -> {
                                Toast.makeText(this, "El usuario ya existe en la base de datos", Toast.LENGTH_LONG).show()
                            }
                            LocalAuthDatabase.RegisterResult.SUCCESS -> {
                                val localToken = "local_token_${System.currentTimeMillis()}"
                                SessionManager.saveToken(this, localToken)
                                Toast.makeText(this, "Registro local exitoso", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }
                            else -> handleApiError(error, authenticated = false)
                        }
                    },
                    onLoading = { loading ->
                        binding.buttonRegister.isEnabled = !loading
                        binding.buttonToLogin.isEnabled = !loading
                    }
                )
            }
        }

        binding.buttonToLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val name = binding.inputName.text?.toString()?.trim().orEmpty()
        val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.text?.toString()?.trim().orEmpty()

        if (name.isEmpty()) {
            binding.layoutName.error = "El nombre es obligatorio"
            isValid = false
        } else {
            binding.layoutName.error = null
        }

        if (email.isEmpty()) {
            binding.layoutEmail.error = "El correo electrónico es obligatorio"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.error = "Introduce un correo electrónico válido"
            isValid = false
        } else {
            binding.layoutEmail.error = null
        }

        if (password.isEmpty()) {
            binding.layoutPassword.error = "La contraseña es obligatoria"
            isValid = false
        } else if (password.length < 6) {
            binding.layoutPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            binding.layoutPassword.error = null
        }

        return isValid
    }
}
