package yz.li.domain.paging.repo

import yz.li.domain.database.api.RemoteRepoApi
import yz.li.domain.database.dao.RemoteDao
import yz.li.domain.database.entities.RemoteEntity
import javax.inject.Inject

/**
 * desc:
 * created by liyuzheng on 2026/4/9 17:37
 */
internal class RemoteRepo @Inject constructor(private val dao: RemoteDao) : RemoteRepoApi {
    override suspend fun insertAsync(remote: RemoteEntity) {
        dao.insertAsync(remote)
    }

    override suspend fun getRemoteKeysAsync(remoteName: String): RemoteEntity? =
        dao.getRemoteKeysAsync(remoteName)

    override suspend fun clearRemoteKeys(remoteName: String) {
        dao.clearRemoteKeys(remoteName)
    }
}