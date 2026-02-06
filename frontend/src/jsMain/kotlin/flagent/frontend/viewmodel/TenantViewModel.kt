package flagent.frontend.viewmodel

import androidx.compose.runtime.*
import flagent.frontend.api.ApiClient
import flagent.frontend.api.CreateTenantRequest
import flagent.frontend.api.TenantResponse
import flagent.frontend.state.Tenant
import flagent.frontend.util.AppLogger
import flagent.frontend.util.ErrorHandler
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * ViewModel for Multi-tenancy (Phase 2)
 */
class TenantViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "TenantViewModel"
    
    private val TENANT_KEY = "current_tenant"
    private val API_KEY_KEY = "api_key"
    private val TENANT_API_KEYS_KEY = "tenant_api_keys"
    
    var currentTenant by mutableStateOf<Tenant?>(null)
        private set
    
    var tenants by mutableStateOf<List<Tenant>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    init {
        loadTenantState()
    }
    
    private fun loadTenantState() {
        val savedTenantId = localStorage[TENANT_KEY]
        if (savedTenantId != null && savedTenantId.isNotBlank()) {
            // Restore current tenant from list when loadTenants() is called
            currentTenant = tenants.find { it.id == savedTenantId } ?: currentTenant
        }
    }
    
    fun loadTenants() {
        scope.launch {
            isLoading = true
            error = null
            
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Loading tenants")
                    val list = ApiClient.getTenants(includeDeleted = false)
                    tenants = list.map { it.toTenant() }
                    loadTenantState()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
            
            isLoading = false
        }
    }
    
    fun createTenant(key: String, name: String, plan: String, ownerEmail: String, onSuccess: (Tenant, String) -> Unit = { _, _ -> }) {
        scope.launch {
            isLoading = true
            error = null
            
            ErrorHandler.withErrorHandling(
                block = {
                    AppLogger.info(TAG, "Creating tenant: $key")
                    val request = CreateTenantRequest(key = key, name = name, plan = plan, ownerEmail = ownerEmail)
                    val response = ApiClient.createTenant(request)
                    val tenant = response.tenant.toTenant()
                    tenants = tenants + tenant
                    onSuccess(tenant, response.apiKey)
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
            
            isLoading = false
        }
    }
    
    fun deleteTenant(id: Long, immediate: Boolean = false, onSuccess: () -> Unit = {}) {
        scope.launch {
            ErrorHandler.withErrorHandling(
                block = {
                    ApiClient.deleteTenant(id, immediate)
                    tenants = tenants.filter { it.id != id.toString() }
                    if (currentTenant?.id == id.toString()) currentTenant = null
                    onSuccess()
                },
                onError = { err ->
                    error = ErrorHandler.getUserMessage(err)
                }
            )
        }
    }
    
    fun switchTenant(tenant: Tenant) {
        currentTenant = tenant
        localStorage[TENANT_KEY] = tenant.id
        getApiKeyForTenant(tenant.id)?.let { apiKey ->
            localStorage[API_KEY_KEY] = apiKey
        }
        AppLogger.info(TAG, "Switched to tenant: ${tenant.name}")
    }

    fun storeApiKeyForTenant(tenantId: String, apiKey: String) {
        val map = parseTenantApiKeys(localStorage[TENANT_API_KEYS_KEY])
        val updated = buildJsonObject { map.forEach { put(it.key, it.value) }; put(tenantId, apiKey) }
        localStorage[TENANT_API_KEYS_KEY] = updated.toString()
    }

    private fun getApiKeyForTenant(tenantId: String): String? =
        parseTenantApiKeys(localStorage[TENANT_API_KEYS_KEY])[tenantId]

    private fun parseTenantApiKeys(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = Json.parseToJsonElement(raw) as? JsonObject ?: return@runCatching emptyMap()
            obj.mapValues { (_, v) -> v.jsonPrimitive.content }
        }.getOrElse { emptyMap() }
    }
    
    fun getApiKey(): String? {
        return localStorage[API_KEY_KEY]
    }
    
    fun getTenantId(): String? {
        return currentTenant?.id
    }
    
    fun clearError() {
        error = null
    }
}

private fun TenantResponse.toTenant() = Tenant(
    id = id.toString(),
    key = key,
    name = name,
    plan = plan,
    status = status
)
