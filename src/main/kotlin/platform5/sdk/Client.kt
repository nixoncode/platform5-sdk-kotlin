package platform5.sdk

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.UUID

open class Client(
    private val apiKey: String,
    private val baseUrl: String = "http://localhost:8084",
) {
    private val json = Json { ignoreUnknownKeys = true }
    protected open val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        expectSuccess = false
    }

    suspend inline fun <reified T> request(
        method: HttpMethod,
        path: String,
        body: Any? = null,
        idempotencyKey: String? = null,
    ): T? {
        val response = httpClient.request(baseUrl.trimEnd('/') + path) {
            this.method = method
            header("X-API-Key", apiKey)
            header("Content-Type", "application/json")
            if (idempotencyKey != null) {
                header("Idempotency-Key", idempotencyKey)
            }
            if (body != null) {
                setBody(body)
            }
        }

        val requestId = response.headers["X-Request-ID"]
        val statusCode = response.status.value

        if (statusCode >= 400) {
            throw toError(statusCode, response, requestId)
        }

        val envelope = response.body<ApiResponse>()
        val data = envelope.data ?: return null
        return json.decodeFromJsonElement<T>(data)
    }

    private suspend fun toError(statusCode: Int, response: HttpResponse, requestId: String?): Platform5Exception {
        val body = try {
            response.body<ApiResponse>()
        } catch (_: Exception) {
            null
        }
        val message = body?.message ?: response.status.description
        val errors = body?.errors

        return when (statusCode) {
            401 -> Platform5Exception.Unauthorized(message, errors, requestId)
            402 -> Platform5Exception.InsufficientBalance(message, errors, requestId)
            403 -> Platform5Exception.Forbidden(message, errors, requestId)
            404 -> Platform5Exception.NotFound(message, errors, requestId)
            422 -> Platform5Exception.Validation(message, errors, requestId)
            429 -> {
                val limit = response.headers["X-RateLimit-Limit"]?.toIntOrNull() ?: 0
                val remaining = response.headers["X-RateLimit-Remaining"]?.toIntOrNull() ?: 0
                Platform5Exception.RateLimit(message, errors, requestId, limit, remaining)
            }
            else -> Platform5Exception.Generic(message, statusCode, errors, requestId)
        }
    }

    fun uuid(): String = UUID.randomUUID().toString()
}
