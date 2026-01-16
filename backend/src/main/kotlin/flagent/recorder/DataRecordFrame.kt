package flagent.recorder

import flagent.config.AppConfig
import flagent.service.EvalResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * DataRecordFrameOptions - options for creating DataRecordFrame
 * Maps to DataRecordFrameOptions from pkg/handler/data_record_frame.go
 */
data class DataRecordFrameOptions(
    val encrypted: Boolean = false,
    val encryptionKey: String? = null,
    val frameOutputMode: String = "payload_string"
)

/**
 * DataRecordFrame - structure for JSON marshaling into data recorders
 * Maps to DataRecordFrame from pkg/handler/data_record_frame.go
 */
class DataRecordFrame(
    private val evalResult: EvalResult,
    private val options: DataRecordFrameOptions
) {
    companion object {
        const val FRAME_OUTPUT_MODE_PAYLOAD_RAW_JSON = "payload_raw_json"
        const val FRAME_OUTPUT_MODE_PAYLOAD_STRING = "payload_string"
    }
    
    /**
     * Get partition key from entityID
     */
    fun getPartitionKey(): String {
        return evalResult.evalContext.entityID ?: ""
    }
    
    /**
     * Output JSON marshaled bytes
     */
    fun output(): ByteArray {
        val json = Json { 
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val payloadJson = json.encodeToJsonElement(EvalResult.serializer(), evalResult)
        val payloadString = json.encodeToString(EvalResult.serializer(), evalResult)
        
        return when (options.frameOutputMode) {
            FRAME_OUTPUT_MODE_PAYLOAD_RAW_JSON -> {
                val rawPayload = buildJsonObject {
                    put("payload", payloadJson)
                }
                json.encodeToString(JsonObject.serializer(), rawPayload).toByteArray()
            }
            else -> {
                val encryptedPayload = if (options.encrypted && options.encryptionKey != null) {
                    encrypt(payloadString, options.encryptionKey)
                } else {
                    payloadString
                }
                
                val stringPayload = buildJsonObject {
                    put("payload", encryptedPayload)
                    put("encrypted", options.encrypted && options.encryptionKey != null)
                }
                json.encodeToString(JsonObject.serializer(), stringPayload).toByteArray()
            }
        }
    }
    
    private fun encrypt(data: String, key: String): String {
        // Simple encryption using AES (similar to simplebox in Go)
        // For production, consider using a more secure encryption library
        try {
            val keyBytes = key.toByteArray().copyOf(32) // AES-256 requires 32 bytes
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encrypted = cipher.doFinal(data.toByteArray())
            return Base64.getEncoder().encodeToString(encrypted)
        } catch (e: Exception) {
            throw RuntimeException("Failed to encrypt data", e)
        }
    }
}
