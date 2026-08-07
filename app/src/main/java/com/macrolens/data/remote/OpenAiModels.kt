package com.macrolens.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodEstimate(
    val found: Boolean = true,
    val calories: Int = 0,
    @SerialName("protein_g") val proteinG: Int = 0,
    @SerialName("carbs_g") val carbsG: Int = 0,
    @SerialName("fat_g") val fatG: Int = 0,
    @SerialName("fruit_veg_servings") val fruitVegServings: Int = 0,
    val description: String = ""
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("max_completion_tokens") val maxTokens: Int = 1024
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: List<ContentBlock>
)

@Serializable
sealed class ContentBlock {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : ContentBlock()

    @Serializable
    @SerialName("image_url")
    data class ImageUrl(@SerialName("image_url") val imageUrl: ImageUrlData) : ContentBlock()
}

@Serializable
data class ImageUrlData(
    val url: String,
    val detail: String = "low"
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: ResponseMessage
)

@Serializable
data class ResponseMessage(
    val content: String
)

// Simplified request for text-only messages
@Serializable
data class SimpleMessage(
    val role: String,
    val content: String
)

@Serializable
data class SimpleChatRequest(
    val model: String,
    val messages: List<SimpleMessage>,
    @SerialName("max_completion_tokens") val maxTokens: Int = 256
)
