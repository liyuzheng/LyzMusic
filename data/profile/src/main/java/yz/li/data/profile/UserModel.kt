package yz.li.data.profile

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/20 16:22
 */
@Keep
@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long
)

@Keep
@Serializable
data class UserProfileResponse(
    val data: UserProfile
)

@Keep
@Serializable
data class UserProfile(
    var id: String = "",
    var name: String = "",
    var handle: String = "",
    @SerialName("profile_picture")
    var profilePicture: ProfilePicture? = null
)

@Keep
@Serializable
data class ProfilePicture(
    @SerialName("150x150")
    val px150: String?,
    @SerialName("480x480")
    val px480: String?,
    @SerialName("1000x1000")
    val px1000: String?
)