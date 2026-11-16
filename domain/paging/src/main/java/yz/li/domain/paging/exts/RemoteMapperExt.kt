package yz.li.domain.paging.exts

import yz.li.core_paging.model.RemoteModel
import yz.li.domain.database.entities.RemoteEntity


/**
 * desc:
 * createed by liyuzheng on 2023/8/29 14:52
 */

fun RemoteEntity.toRemoteModel() = RemoteModel(name, next, prepend)
fun RemoteModel.toRemoteEntity() = RemoteEntity(name, next, prepend)