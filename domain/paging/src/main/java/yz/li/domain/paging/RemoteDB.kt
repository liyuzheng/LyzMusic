package yz.li.domain.paging

import yz.li.core_paging.model.RemoteModel
import yz.li.domain.database.RoomDB
import yz.li.domain.database.dao.RemoteDao
import yz.li.domain.paging.exts.toRemoteEntity
import yz.li.domain.paging.exts.toRemoteModel

/**
 * desc:
 * createed by liyuzheng on 2023/8/29 14:51
 */
object RemoteDB {
    private val dao: RemoteDao by lazy {
        RoomDB.INSTANCE.remoteDao()
    }

    suspend fun getRemoteKeysAsync(remoteName: String) =
        dao.getRemoteKeysAsync(remoteName)?.toRemoteModel()

    suspend fun insertAsync(remoteModel: RemoteModel) {
        dao.insertAsync(remoteModel.toRemoteEntity())
    }

    suspend fun clearRemoteKeys(remoteName: String) {
        dao.clearRemoteKeys(remoteName)
    }
}