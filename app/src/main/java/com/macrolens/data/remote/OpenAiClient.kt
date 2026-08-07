package com.macrolens.data.remote

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
                    model = "gpt-5.5",
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

    suspend fun analyzeFoodFromText(apiKey: String, description: String): Result<FoodEstimate> =
        withContext(Dispatchers.IO) {
            runCatching {
                val prompt = """
                    The user described food they are about to eat: "$description"

                    Estimate the nutritional content based on this description.

                    IMPORTANT: You MUST respond with ONLY a valid JSON object, no other text.

                    If it is food, respond with:
                    {"found":true,"calories":300,"protein_g":25,"carbs_g":30,"fat_g":12,"fruit_veg_servings":0,"description":"grilled chicken breast"}

                    If the description is clearly not food, respond with:
                    {"found":false,"description":"not food"}

                    Rules:
                    - Honor exact quantities if given (e.g. "16oz", "2 cups", "large", "half")
                    - All numeric values are integers
                    - fruit_veg_servings: count of fruit/vegetable servings (1 serving = 1 medium piece of fruit, 1/2 cup chopped fruit or veg, 1 cup leafy greens). Use 0 if none.
                    - description: clean 2-5 word label for the food logged
                    - NEVER respond with plain text, ONLY JSON
                """.trimIndent()

                val request = SimpleChatRequest(
                    model = "gpt-5.5",
                    messages = listOf(SimpleMessage(role = "user", content = prompt))
                )

                val response = post(apiKey, json.encodeToString(request))
                val content = response.choices.first().message.content
                val estimate = json.decodeFromString<FoodEstimate>(extractJson(content))

                if (!estimate.found) {
                    throw NoFoodFoundException(estimate.description.ifEmpty { "Not recognized as food" })
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
                model = "gpt-5.5",
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
            You are a nutrition analyzer. Look at this image and determine the total nutritional content of the food the user is about to eat.

            IMPORTANT: You MUST respond with ONLY a valid JSON object, no other text.

            PACKAGED FOOD / NUTRITION LABELS (highest priority):
            - If you see a nutrition label, READ the values directly from it — do not estimate.
            - Assume the user will eat the ENTIRE package or container unless it is obviously a bulk/family-size item (e.g., a full gallon of milk, a large box of cereal).
            - If the label lists "servings per container" > 1, multiply the per-serving values by that number to get the whole-container total.
            - A single-serve container (yogurt cup, protein bar, drink bottle, snack bag) should be treated as one full serving.

            PREPARED / PLATED FOOD:
            - Estimate based on what is visible on the plate or in the bowl.
            - Use typical portion sizes when the quantity is unclear.

            RESPONSE FORMAT:
            If food or a food package is visible (including a nutrition label):
            {"found":true,"calories":300,"protein_g":25,"carbs_g":30,"fat_g":12,"fruit_veg_servings":2,"description":"Chobani plain yogurt (whole container)"}

            If the image clearly contains NO food and NO food packaging (e.g., a person, landscape, non-food object):
            {"found":false,"description":"what you see, e.g. 'a coffee mug'"}

            Rules:
            - description: concise label, note if it's a whole container
            - All numeric values are integers
            - fruit_veg_servings: count of fruit/vegetable servings (1 serving = 1 medium piece of fruit, 1/2 cup chopped fruit or veg, or 1 cup leafy greens). Use 0 for foods with no meaningful fruit or vegetable content.
            - NEVER respond with plain text, ONLY JSON
        """.trimIndent()
    }
}
