package yz.li.domain.network.auth

import com.tencent.mmkv.MMKV
import yz.li.core_tools.utils.MMKVReadWrite

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/20 16:55
 */
object AuthMMKV {
    private val mmkv by lazy {
        MMKV.mmkvWithID("AuthStore")
    }
    var codeVerifier by AuthMMKV("pkce_verifier", "")
    var accessToken by AuthMMKV("access_token", "")
    var refreshToken by AuthMMKV("refresh_token", "")
    var expiresIn by AuthMMKV("expires_in", 0)
    var refreshExpiresIn by AuthMMKV("refresh_expires_in", 0)
    var tokenType by AuthMMKV("token_type", "")

    class AuthMMKV<T>(key: String, defaultValue: T) : MMKVReadWrite<T>(key, defaultValue) {
        override fun getMMKV(): MMKV = mmkv
    }
}