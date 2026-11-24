package yz.li.core_network


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class Repo {
    companion object {
        private var retrofitClient: IRetrofitClient = RetrofitClient()
        fun registerRetrofitClient(client: IRetrofitClient) {
            retrofitClient = client
        }
    }

    var req: BaseRequest = BaseRequest()

    private fun injectApiService(): Api {
        return retrofitClient.provideRetrofit().create(Api::class.java)
    }

    suspend fun execute() = withContext(Dispatchers.IO) {
        val request = req
        val params = request.reflectParameters()
        val apiService = injectApiService()

        val api = request.api ?: throw RuntimeException("repo没有传入地址")
        when (request.requestMode) {
            RequestMode.MULTIPART -> {
                val requestBody: RequestBody =
                    request.file?.asRequestBody(request.contentType)
                        ?: throw RuntimeException("execute MULTIPART 时，file 不能为空")
                apiService.uploadFile(
                    api,
                    MultipartBody.Part.createFormData(
                        "uploadFile",
                        request.file?.name,
                        requestBody
                    ),
                )
            }

            RequestMode.POST -> {
                apiService.post(
                    api,
                    buildRequestBody(params, request.contentType)
                )
            }

            RequestMode.GET -> {
                apiService.get(
                    api,
                    buildQueryMap(params)
                )
            }

            RequestMode.PUT -> {
                apiService.put(
                    api,
                    buildRequestBody(params, request.contentType)
                )
            }

            RequestMode.DELETE -> {
                apiService.delete(
                    api,
                    buildQueryMap(params)
                )
            }

            RequestMode.DELETE_BODY -> {
                apiService.deleteBody(
                    api,
                    buildRequestBody(params, request.contentType)
                )
            }

            else -> {
                throw UnsupportedOperationException("不支持的requestMode")
            }
        }
    }

    /**
     * 过滤 GET / DELETE Query 参数中的 null 值，防止 Retrofit @QueryMap 报错
     */
    private fun buildQueryMap(body: MutableMap<String, Any?>): MutableMap<String, Any?> {
        return body.filterValues { it != null }.toMutableMap()
    }

    /**
     * 根据 contentType 自动构建 FormBody (表单) 或 JSON RequestBody
     */
    private fun buildRequestBody(
        params: MutableMap<String, Any?>,
        contentType: MediaType?
    ): RequestBody {
        val mediaType = contentType ?: "application/json; charset=utf-8".toMediaType()

        // 1. 表单模式 (application/x-www-form-urlencoded) -> 适用于 Audius OAuth 换取 Token
        if (mediaType.type == "application" && mediaType.subtype == "x-www-form-urlencoded") {
            return FormBody.Builder().apply {
                params.forEach { (key, value) ->
                    if (value != null) {
                        add(key, value.toString())
                    }
                }
            }.build()
        }

        // 2. JSON 模式 (默认 application/json) -> 将 Map<String, Any?> 安全转换为 JsonObject 字符串
        val jsonString = params.toJsonElement().toString()
        return jsonString.toRequestBody(mediaType)
    }

    /**
     * 将任意 Map / List / 基础类型安全转换为 kotlinx.serialization 的 JsonElement
     */
    private fun Any?.toJsonElement(): JsonElement {
        return when (this) {
            null -> JsonNull
            is JsonElement -> this
            is Boolean -> JsonPrimitive(this)
            is Number -> JsonPrimitive(this)
            is String -> JsonPrimitive(this)
            is Map<*, *> -> {
                val content = LinkedHashMap<String, JsonElement>()
                for ((k, v) in this) {
                    if (k != null) {
                        content[k.toString()] = v.toJsonElement()
                    }
                }
                JsonObject(content)
            }

            is Iterable<*> -> JsonArray(this.map { it.toJsonElement() })
            is Array<*> -> JsonArray(this.map { it.toJsonElement() })
            else -> JsonPrimitive(this.toString())
        }
    }
}
