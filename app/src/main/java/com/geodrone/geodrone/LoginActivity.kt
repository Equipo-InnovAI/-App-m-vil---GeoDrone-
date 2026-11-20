package com.geodrone.geodrone

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.geodrone.geodrone.api.ApiActivity
import com.geodrone.geodrone.api.ApiClient
import com.geodrone.geodrone.api.LoginRequest
import com.geodrone.geodrone.api.SessionManager
import com.geodrone.geodrone.databinding.ActivityLoginBinding
import com.geodrone.geodrone.db.LocalAuthDatabase

class LoginActivity : ApiActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!SessionManager.token(this).isNullOrBlank()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        binding.buttonLogin.setOnClickListener {
            if (validateInputs()) {
                val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
                val password = binding.inputPassword.text?.toString().orEmpty()

                val localDb = LocalAuthDatabase(this)

                executeApi(
                    request = { ApiClient.service.login(LoginRequest(email, password)) },
                    onSuccess = { response ->
                        localDb.registerUser(email.substringBefore('@'), email, password)
                        val token = response.authToken
                        if (token.isNullOrBlank()) {
                            handleApiError(IllegalStateException("El servidor no devolvió un token de acceso"), authenticated = false)
                        } else {
                            SessionManager.saveToken(this, token)
                            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                    },
                    onError = { error ->
                        val loginResult = localDb.loginUser(email, password)
                        when (loginResult) {
                            LocalAuthDatabase.LoginResult.USER_NOT_FOUND -> {
                                Toast.makeText(this, "El usuario no existe. Regístrate primero.", Toast.LENGTH_LONG).show()
                            }
                            LocalAuthDatabase.LoginResult.WRONG_PASSWORD -> {
                                Toast.makeText(this, "La contraseña es errónea.", Toast.LENGTH_LONG).show()
                            }
                            LocalAuthDatabase.LoginResult.SUCCESS -> {
                                val localToken = "local_token_${System.currentTimeMillis()}"
                                SessionManager.saveToken(this, localToken)
                                Toast.makeText(this, "Inicio de sesión local exitoso", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }
                            else -> handleApiError(error, authenticated = false)
                        }
                    },
                    onLoading = { loading ->
                        binding.buttonLogin.isEnabled = !loading
                        binding.buttonToRegister.isEnabled = !loading
                    }
                )
            }
        }

        binding.buttonToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.text?.toString()?.trim().orEmpty()

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
        } else {
            binding.layoutPassword.error = null
        }

        return isValid
    }
}
