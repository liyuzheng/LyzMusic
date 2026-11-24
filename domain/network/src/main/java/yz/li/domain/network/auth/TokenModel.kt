package yz.li.domain.network.auth

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import yz.li.core_network.BaseResponse

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/24 17:09
 */
@Keep
@Serializable
data class TokenModel(
    @SerialName("access_token")
    var accessToken: String = "",
    @SerialName("refresh_token")
    var refreshToken: String = "",
    @SerialName("expires_in")
    var expiresIn: Int = 0,
    @SerialName("refresh_expires_in")
    var refreshExpiresIn: Int = 0,
    @SerialName("token_type")
    var tokenType: String = ""
) : BaseResponse()