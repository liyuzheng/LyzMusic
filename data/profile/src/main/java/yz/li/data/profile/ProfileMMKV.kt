package yz.li.data.profile

import com.tencent.mmkv.MMKV
import yz.li.core_tools.utils.MMKVReadWrite

/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/20 16:21
 */
object ProfileMMKV {
    private val mmkv by lazy {
        MMKV.mmkvWithID("UserStore")
    }

    var currentUser by ProfileMMKV("login.user", UserProfile())
    var token by ProfileMMKV("login.token", "")

    class ProfileMMKV<T>(key: String, defaultValue: T) : MMKVReadWrite<T>(key, defaultValue) {
        override fun getMMKV(): MMKV = mmkv
    }
}