package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response models for Moshi ---

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiResponseFormat(
    @Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)


// --- Parsed AI Output Models ---

@JsonClass(generateAdapter = true)
data class AIEvaluationResult(
    val status_id: Int, // 3=Accepted, 4=Wrong Answer, 6=Compile Error, 11=Runtime Error
    val status_description: String, // "Accepted", "Wrong Answer", etc.
    val stdout: String?,
    val stderr: String?,
    val compile_output: String?,
    val explanation: String // AI insights on why it succeeded or failed and suggestions for improvement!
)

@JsonClass(generateAdapter = true)
data class AIInsightsResult(
    val weakTopics: List<String>,
    val recommendation: String,
    val feedbackMessage: String
)

// --- Retrofit Endpoint Definition ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Client Factory ---

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    // --- Developer helper tasks ---

    /**
     * Fallback Code Compilation & Execution check using Gemini.
     */
    suspend fun evaluateCodeWithAI(
        problemTitle: String,
        problemDesc: String,
        language: String,
        code: String,
        testCasesJson: String
    ): AIEvaluationResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return AIEvaluationResult(
                status_id = 6,
                status_description = "Configuration Error",
                stdout = null,
                stderr = "Gemini API Key is empty or invalid. Please check the Secrets panel inside your AI Studio.",
                compile_output = "Unable to evaluate code: Gemini API Key missing.",
                explanation = "Your AI Studio environment does not have a configured GEMINI_API_KEY. Configure it via the secrets drawer."
            )
        }

        val prompt = """
            Evaluate and run the following user-submitted code.
            
            [PROBLEM TITLE]:
            $problemTitle
            
            [PROBLEM DESCRIPTION]:
            $problemDesc
            
            [TEST CASES]:
            $testCasesJson
            
            [USER SUBMITTED CODE in $language]:
            $code
            
            Perform a logical evaluation. Test it against the provided test cases. Determine if it compiles successfully, if it yields correct results, or if there is a flaw, runtime bug, or off-by-one edgecase error.
            
            Return a JSON object conforming to the following schema:
            {
               "status_id": 3,
               "status_description": "Accepted",
               "stdout": "Sample outputs from execution",
               "stderr": "Null or error trace if runtime failed",
               "compile_output": "Null if compile ok, otherwise syntax message",
               "explanation": "Brief, clear explanation of constraints met, space/time complexity, or suggestions if failed"
            }
            
            Use standard status_id categories:
            - 3: Accepted (code correctly solves all test cases)
            - 4: Wrong Answer (returns incorrect results for some cases)
            - 6: Compile Error (cannot run due to syntax/type issues)
            - 11: Runtime Error (array index bounds, division-by-zero, exception, infinite loop timeout)
            
            Respond with raw JSON only. Do not wrap in markdown ```json or backticks. It must be directly parseable.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are a sandboxed LeetCode compiler and logic validator that outputs strictly structured JSON.")))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty reaction from AI Model.")
            
            moshi.adapter(AIEvaluationResult::class.java).fromJson(jsonText)
                ?: throw Exception("Failed to deserialize compilation response.")
        } catch (e: Exception) {
            AIEvaluationResult(
                status_id = 11,
                status_description = "Execution Timeout",
                stdout = null,
                stderr = e.localizedMessage,
                compile_output = "Retrofit Error: Connective state broken.",
                explanation = "The compilation pipeline timed out or met network anomalies: ${e.message}"
            )
        }
    }

    /**
     * AI performance analyzer.
     */
    suspend fun getPerformanceInsights(submissionsHistoryJson: String): AIInsightsResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return AIInsightsResult(
                weakTopics = listOf("No API Key Setup"),
                recommendation = "Add a valid GEMINI_API_KEY inside the secure secrets drawer of Google AI Studio.",
                feedbackMessage = "Setup GEMINI_API_KEY to configure automatic weak topic identification, code profiling, and customizable tips."
            )
        }

        val prompt = """
            Analyze the following submission history JSON for a coding student:
            $submissionsHistoryJson
            
            Identify any persistent weak topics (such as DP, Arrays, String Manipulation, Trees, or Complexity oversights), count failure percentages, and provide structured personal guidance.
            
            Return a JSON object conforming to the following scheme:
            {
              "weakTopics": ["Array Manipulation", "Hash Mapping"],
              "recommendation": "Practice more sliding window tasks and revise key-value structures. Dedicate 20 minutes to basic lookup hashing.",
              "feedbackMessage": "Great effort! You solved 4 medium problems this week, but seem to struggle with O(N) runtime compliance in Easy Hash collections."
            }
            
            Do not include markdown or text wrapping. Only raw JSON.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.5f
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are an expert curriculum builder and developer coach.")))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No response.")
            moshi.adapter(AIInsightsResult::class.java).fromJson(jsonText)
                ?: throw Exception("Deserialization issue.")
        } catch (e: Exception) {
            AIInsightsResult(
                weakTopics = listOf("Network error"),
                recommendation = "Retry loading recommendations shortly.",
                feedbackMessage = "Curriculum profiling failed due to: ${e.localizedMessage}"
            )
        }
    }

    /**
     * LinkedIn Post Generator.
     */
    suspend fun generateLinkedInContent(
        userName: String,
        solvedCountToday: Int,
        solvedProblems: List<String>,
        streak: Int
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "🚀 Tracking my coding journey on CodeQuest AI! Today I solved some amazing algorithms and am active on my practice streak. #CodeQuestAI #DeveloperJourney #LeetCode"
        }

        val prompt = """
            Draw an energetic, highly engaging, and professional LinkedIn progress post for a software engineer.
            
            - Engineer Name: $userName
            - Today's Solved Coding Problems: ${solvedProblems.joinToString()}
            - Active Streak: $streak Days
            - Today's solves count: $solvedCountToday problems!
            
            Include tech-positive bullet points detailing what they learned or implemented (e.g. hash mappings, binary search optimization, brute force optimizations). Keep it crisp, highly professional, exciting, and include relevant developer hashtags. 
            Do not include any placeholders like [Insert Name Here] or markdown formatting that wouldn't fit directly on a mobile share sheet. Text ONLY.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Failed to generate customized post. Check details and resubmit!"
        } catch (e: Exception) {
            "🚀 Celebrating a new step in my coding journey with CodeQuest AI! Solving problems everyday, staying consistent on my $streak-day streak. #DeveloperSuccess #Java #Kotlin #CodingSkills"
        }
    }
}
