package com.example.data.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Judge0Request(
    val source_code: String,
    val language_id: Int,
    val stdin: String? = null,
    val expected_output: String? = null
)

@JsonClass(generateAdapter = true)
data class Judge0Status(
    val id: Int,
    val description: String // "Accepted", "Wrong Answer", "Compilation Error", etc.
)

@JsonClass(generateAdapter = true)
data class Judge0Response(
    val stdout: String? = null,
    val time: String? = null,
    val stderr: String? = null,
    val compile_output: String? = null,
    val message: String? = null,
    val status: Judge0Status? = null
)

interface JudgeApiService {
    @POST("submissions")
    suspend fun createSubmission(
        @Query("base64_encoded") base64: Boolean = false,
        @Query("wait") wait: Boolean = true,
        @Header("X-RapidAPI-Host") host: String,
        @Header("X-RapidAPI-Key") apiKey: String,
        @Body request: Judge0Request
    ): Judge0Response
}

object Judge0Client {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Build dynamic compiler based on custom user configured host/endpoints!
     */
    fun getApiService(customBaseUrl: String): JudgeApiService {
        val cleanUrl = if (customBaseUrl.endsWith("/")) customBaseUrl else "$customBaseUrl/"
        return Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JudgeApiService::class.java)
    }
}
