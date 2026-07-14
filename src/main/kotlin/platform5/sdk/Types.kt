package platform5.sdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SendSMSRequest(
    val to: String,
    val message: String,
    val from: String,
)

@Serializable
data class SendSMSResponse(
    @SerialName("message_id") val messageId: String,
    val to: String,
    @SerialName("sender_name") val senderName: String,
    val parts: Int,
    val cost: Double,
    val currency: String,
    val status: String,
)

@Serializable
data class SendEmailRequest(
    val to: String,
    val subject: String,
    val body: String,
    @SerialName("body_type") val bodyType: String? = null,
    val from: String,
)

@Serializable
data class SendEmailResponse(
    @SerialName("message_id") val messageId: String,
    val status: String,
)

@Serializable
data class MessageStatusResponse(
    val id: String,
    val to: String,
    @SerialName("sender_name") val senderName: String,
    val parts: Int,
    val cost: Double,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("sent_at") val sentAt: String? = null,
    @SerialName("delivered_at") val deliveredAt: String? = null,
    val error: String? = null,
)

@Serializable
data class BalanceResponse(
    @SerialName("available_balance") val availableBalance: Double,
    @SerialName("current_balance") val currentBalance: Double,
    val currency: String,
)

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: JsonElement? = null,
    val errors: String? = null,
)
