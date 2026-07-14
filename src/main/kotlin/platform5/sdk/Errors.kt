package platform5.sdk

sealed class Platform5Exception(
    message: String,
    val statusCode: Int,
    val errors: String? = null,
    val requestId: String? = null,
) : Exception(message) {
    class Unauthorized(message: String, errors: String? = null, requestId: String? = null) :
        Platform5Exception(message, 401, errors, requestId)

    class InsufficientBalance(message: String, errors: String? = null, requestId: String? = null) :
        Platform5Exception(message, 402, errors, requestId)

    class Forbidden(message: String, errors: String? = null, requestId: String? = null) :
        Platform5Exception(message, 403, errors, requestId)

    class NotFound(message: String, errors: String? = null, requestId: String? = null) :
        Platform5Exception(message, 404, errors, requestId)

    class Validation(message: String, errors: String? = null, requestId: String? = null) :
        Platform5Exception(message, 422, errors, requestId)

    class RateLimit(
        message: String,
        errors: String? = null,
        requestId: String? = null,
        val limit: Int = 0,
        val remaining: Int = 0,
    ) : Platform5Exception(message, 429, errors, requestId)

    class Generic(message: String, statusCode: Int, errors: String? = null, requestId: String? = null) :
        Platform5Exception(message, statusCode, errors, requestId)
}
