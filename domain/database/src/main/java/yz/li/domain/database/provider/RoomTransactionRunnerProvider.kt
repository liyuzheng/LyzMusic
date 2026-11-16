package yz.li.domain.database.provider

import androidx.room.withTransaction
import yz.li.domain.database.AppDatabase
import yz.li.domain.database.api.DatabaseTransactionRunner
import javax.inject.Inject

/**
 * desc:
 * created by liyuzheng on 2026/4/9 16:59
 */
class RoomTransactionRunnerProvider @Inject constructor(private val db: AppDatabase) :
    DatabaseTransactionRunner {
    override suspend fun <T> invoke(block: suspend () -> T): T {
        return db.withTransaction { block() }
    }
}