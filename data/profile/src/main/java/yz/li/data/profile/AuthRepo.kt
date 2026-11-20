package yz.li.data.profile

import android.net.Uri
import android.util.Base64
import timber.log.Timber
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/20 16:48
 */
class AuthRepo {
    private val clientId = "ec5f04ce96d06c444d27a691bb4dfd19e4f51f4f"
    private val redirectUri = "lyzmusic://oauth/callback"

    fun buildLoginUri(): Uri {
        val verifier = generateCodeVerifier()
        VerifierMMKV.verifier = verifier
        val challenge = generateCodeChallenge(verifier)

        val authUri = Uri.Builder()
            .scheme("https")
            .authority("audius.co")
            .path("/oauth/auth")
            .appendQueryParameter("scope", "write")
            .appendQueryParameter("api_key", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
        Timber.v("c: $authUri")
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
}