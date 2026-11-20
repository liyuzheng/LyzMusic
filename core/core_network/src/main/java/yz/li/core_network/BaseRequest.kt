package yz.li.core_network

import okhttp3.MediaType
import java.io.File

class BaseRequest {
    var api: String? = null

    var requestMode: Int = RequestMode.GET

    var file: File? = null

    private var params = mutableMapOf<String, Any?>()

    var contentType: MediaType? = null

    fun api(value: String) {
        api = value
    }

    fun requestMode(value: Int) {
        requestMode = value
    }

    fun file(init: () -> File) {
        file = init()
    }

    fun params(init: () -> Pair<String, Any?>) {
        val p = init()
        params[p.first] = p.second
    }

    fun contentType(init: () -> MediaType?) {
        contentType = init()
    }

    override fun toString(): String {
        return "api:$api \n params :$params"
    }

    fun reflectParameters(): MutableMap<String, Any?> {
        return params
    }

}