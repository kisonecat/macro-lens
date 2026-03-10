package com.phood.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenAiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun analyzeFood(apiKey: String, imageBase64: String): Result<FoodEstimate> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = ChatRequest(
                    model = "gpt-4o",
                    messages = listOf(
                        ChatMessage(
                            role = "user",
                            content = listOf(
                                ContentBlock.Text(FOOD_ANALYSIS_PROMPT),
                                ContentBlock.ImageUrl(
                                    ImageUrlData(url = "data:image/jpeg;base64,$imageBase64")
                                )
                            )
                        )
                    )
                )

                val response = post(apiKey, json.encodeToString(request))
                val content = response.choices.first().message.content

                // Extract JSON from response (may be wrapped in markdown code block)
                val jsonString = extractJson(content)
                val estimate = json.decodeFromString<FoodEstimate>(jsonString)

                // Check if food was actually found
                if (!estimate.found) {
                    throw NoFoodFoundException(estimate.description.ifEmpty { "No food detected in image" })
                }

                estimate
            }
        }

    class NoFoodFoundException(message: String) : Exception(message)

    suspend fun getInspirationalMessage(
        apiKey: String,
        customPrompt: String,
        calories: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = SimpleChatRequest(
                model = "gpt-4o",
                messages = listOf(
                    SimpleMessage(
                        role = "system",
                        content = "You are a friendly nutrition coach. Keep responses brief (1-2 sentences)."
                    ),
                    SimpleMessage(
                        role = "user",
                        content = """
                            Today's totals so far:
                            - Calories: $calories
                            - Protein: ${proteinG}g
                            - Carbs: ${carbsG}g
                            - Fat: ${fatG}g

                            $customPrompt
                        """.trimIndent()
                    )
                )
            )

            val response = post(apiKey, json.encodeToString(request))
            response.choices.first().message.content.trim()
        }
    }

    private fun post(apiKey: String, body: String): ChatResponse {
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("OpenAI API error: ${response.code} ${response.body?.string()}")
        }

        return json.decodeFromString(response.body!!.string())
    }

    private fun extractJson(content: String): String {
        // Handle markdown code blocks
        val codeBlockRegex = """```(?:json)?\s*([\s\S]*?)\s*```""".toRegex()
        val match = codeBlockRegex.find(content)
        if (match != null) {
            return match.groupValues[1].trim()
        }

        // Try to find raw JSON object
        val jsonStart = content.indexOf('{')
        val jsonEnd = content.lastIndexOf('}')
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd + 1)
        }

        return content.trim()
    }

    companion object {
        private val FOOD_ANALYSIS_PROMPT = """
            Analyze this image and estimate the nutritional content of any food shown.

            IMPORTANT: You MUST respond with ONLY a valid JSON object, no other text.

            If food is visible, respond with:
            {"found":true,"calories":300,"protein_g":25,"carbs_g":30,"fat_g":12,"description":"grilled chicken salad"}

            If NO food is visible (e.g., a person, landscape, object, etc.), respond with:
            {"found":false,"description":"what you see instead, e.g. 'a desk with papers'"}

            Guidelines:
            - description: 2-5 words describing the food or what's in the image
            - Estimate reasonable values based on typical portion sizes
            - When uncertain, provide your best estimate
            - NEVER respond with plain text, ONLY JSON
        """.trimIndent()
    }
}
