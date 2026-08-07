package com.macrolens.repository

import android.util.Base64
import android.util.Log
import com.macrolens.data.local.FoodEntry
import com.macrolens.data.remote.OpenAiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class FoodAnalysisQueue(
    private val foodRepository: FoodRepository,
    private val llmRepository: LlmRepository
) {
    private val scope = CoroutineScope(SupervisorJob())
    private val mutex = Mutex()

    fun start() {
        scope.launch {
            // Any change to the pending set triggers a drain. Multiple triggers
            // during a drain are safe: the mutex serializes them and each drain
            // exits once the queue is empty.
            foodRepository.observePending().collect { pending ->
                if (pending.isNotEmpty()) drain()
            }
        }
    }

    /** Manually poke the queue (e.g. after a user retry) in case the observer missed it. */
    fun kick() {
        scope.launch { drain() }
    }

    private suspend fun drain() = mutex.withLock {
        while (true) {
            val entry = foodRepository.getFirstPending() ?: return@withLock
            processEntry(entry)
        }
    }

    private suspend fun processEntry(entry: FoodEntry) {
        val imagePath = entry.imagePath
        if (imagePath.isNullOrBlank()) {
            foodRepository.markFailed(entry.id, "Missing image")
            return
        }

        val bytes = runCatching { File(imagePath).readBytes() }.getOrNull()
        if (bytes == null) {
            foodRepository.markFailed(entry.id, "Image file unavailable")
            return
        }

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        var lastError: Throwable? = null
        for (attempt in 1..MAX_ATTEMPTS) {
            val result = llmRepository.analyzeFood(base64)
            result.onSuccess { estimate ->
                foodRepository.markCompleted(entry.id, estimate)
                return
            }.onFailure { error ->
                lastError = error
                if (!isRetryable(error) || attempt == MAX_ATTEMPTS) {
                    foodRepository.markFailed(entry.id, userMessage(error))
                    return
                }
                val backoffMs = INITIAL_BACKOFF_MS shl (attempt - 1)
                Log.w(TAG, "Analysis attempt $attempt failed, retrying in ${backoffMs}ms", error)
                delay(backoffMs)
            }
        }

        foodRepository.markFailed(entry.id, userMessage(lastError))
    }

    private fun isRetryable(error: Throwable): Boolean = when {
        error is OpenAiClient.NoFoodFoundException -> false
        error.message?.contains("API key", ignoreCase = true) == true -> false
        else -> true
    }

    private fun userMessage(error: Throwable?): String = when {
        error is OpenAiClient.NoFoodFoundException ->
            "No food detected${error.message?.let { ": $it" } ?: ""}"
        error?.message?.contains("API key", ignoreCase = true) == true ->
            "API key not configured — set it in Settings, then tap to retry"
        else -> error?.message?.take(140) ?: "Analysis failed"
    }

    companion object {
        private const val TAG = "FoodAnalysisQueue"
        private const val MAX_ATTEMPTS = 3
        private const val INITIAL_BACKOFF_MS = 1_000L
    }
}
