package yz.li.data.profile

import com.tencent.mmkv.MMKV
import yz.li.core_tools.utils.MMKVReadWrite

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/20 16:55
 */
object VerifierMMKV {
    private val mmkv by lazy {
        MMKV.mmkvWithID("VerifierStore")
    }

    var verifier by VerifierMMKV("pkce_verifier", "")

    class VerifierMMKV<T>(key: String, defaultValue: T) : MMKVReadWrite<T>(key, defaultValue) {
        override fun getMMKV(): MMKV = mmkv
    }
}