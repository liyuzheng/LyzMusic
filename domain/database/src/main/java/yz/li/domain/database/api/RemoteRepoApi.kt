package yz.li.domain.database.api

import yz.li.domain.database.entities.RemoteEntity

/**
 * desc:
 * created by liyuzheng on 2026/4/9 17:36
 */
interface RemoteRepoApi {
    suspend fun insertAsync(remote: RemoteEntity)

    suspend fun getRemoteKeysAsync(remoteName: String): RemoteEntity?

    suspend fun clearRemoteKeys(remoteName: String)
}