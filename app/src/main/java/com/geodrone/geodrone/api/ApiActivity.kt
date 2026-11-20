package com.geodrone.geodrone.api

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.geodrone.geodrone.LoginActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

abstract class ApiActivity : AppCompatActivity() {
    protected fun <T> executeApi(
        request: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit = { handleApiError(it) },
        onLoading: (Boolean) -> Unit = {}
    ) {
        lifecycleScope.launch {
            onLoading(true)
            try {
                val result = withContext(Dispatchers.IO) { request() }
                onSuccess(result)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                onError(error)
            } finally {
                onLoading(false)
            }
        }
    }

    protected fun handleApiError(error: Throwable, authenticated: Boolean = true) {
        val httpError = error as? HttpException
        val message = when {
            httpError != null -> {
                val responseBody = httpError.response()?.errorBody()?.string()
                responseBody?.let(::backendErrorMessage)
                    ?: "Error del servidor (${httpError.code()})"
            }
            error is IOException -> "No se pudo conectar con GeoDrone. Revisa que el backend esté activo y la dirección de red sea correcta."
            else -> error.localizedMessage ?: "Ocurrió un error inesperado"
        }

        if (authenticated && httpError?.code() == 401) {
            SessionManager.clear(this)
            Toast.makeText(this, "La sesión venció. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun backendErrorMessage(body: String): String {
        return try {
            JSONObject(body).optString("error").ifBlank { "Error del servidor" }
        } catch (_: JSONException) {
            "Error del servidor"
        }
    }
}
