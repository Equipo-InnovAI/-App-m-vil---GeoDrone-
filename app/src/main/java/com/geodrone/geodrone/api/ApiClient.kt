package com.geodrone.geodrone.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit

object SessionManager {
    private const val PREFERENCES = "geodrone_session"
    private const val TOKEN_KEY = "jwt_token"

    fun token(context: Context): String? =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(TOKEN_KEY, null)

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(TOKEN_KEY, token)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .remove(TOKEN_KEY)
            .apply()
    }
}

object ApiClient {
    // Android emulator maps 10.0.2.2 to the host computer's localhost.
    private const val BASE_URL = "http://10.0.2.2:3005/"

    @Volatile
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    val service: ApiService by lazy {
        val context = checkNotNull(applicationContext) {
            "ApiClient.initialize(context) must be called before accessing the API."
        }
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder().apply {
                SessionManager.token(context)?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }.build()
            chain.proceed(request)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
