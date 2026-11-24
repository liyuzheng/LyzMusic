package yz.li.domain.network.auth

import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import okhttp3.MediaType.Companion.toMediaType
import timber.log.Timber
import yz.li.core_network.RequestMode
import yz.li.core_network.exts.repo
import yz.li.core_network.exts.request
import yz.li.core_network.onCancel
import yz.li.core_network.onCompletion
import yz.li.core_network.onFailure
import yz.li.core_network.onSuccess
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/20 16:48
 */
object AuthRepo {
    public var AUTH_STATE = ""
    private const val CLIENT_ID = "ec5f04ce96d06c444d27a691bb4dfd19e4f51f4f"
    private const val REDIRECT_URI = "lyzmusic://oauth/callback"
    fun buildAuthUri(): Uri {
        val verifier = generateCodeVerifier()
        AuthMMKV.codeVerifier = verifier
        val challenge = generateCodeChallenge(verifier)
        val state = generateOAuthState()
        val authUri = Uri.Builder()
            .scheme("https")
            .authority("audius.co")
            .path("/oauth/auth")
            .appendQueryParameter("scope", "write")
            .appendQueryParameter("api_key", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("state", state)
            .build()
        Timber.v("AuthRepo：$authUri")
        return authUri
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun generateOAuthState(byteLength: Int = 24): String {
        val bytes = ByteArray(byteLength)
        SecureRandom().nextBytes(bytes)
        AUTH_STATE = Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        return AUTH_STATE
    }

    fun exchangeCodeForToken(code: String, coroutineScope: CoroutineScope) {
        val codeVerifier = AuthMMKV.codeVerifier
        repo {
            api("https://api.audius.co/v1/oauth/token")
            requestMode(RequestMode.POST)
            contentType { "application/json".toMediaType() }
            params { "grant_type" to "authorization_code" }
            params { "code" to code }
            params { "code_verifier" to codeVerifier }
            params { "client_id" to CLIENT_ID }
            params { "redirect_uri" to REDIRECT_URI }
        }.request<TokenModel>(coroutineScope) { result ->
            result.onSuccess {
                AuthMMKV.refreshToken = it.refreshToken
                AuthMMKV.accessToken = it.accessToken
                AuthMMKV.expiresIn = it.expiresIn
                AuthMMKV.refreshExpiresIn = it.refreshExpiresIn
                AuthMMKV.tokenType = it.tokenType
            }
            result.onCancel {
                Timber.v("onCancel :onCancel")
            }
            result.onFailure {
                Timber.e("error :$it")
            }
            result.onCompletion {
                Timber.v("onCompletion :onCompletion")
            }
        }
    }
}