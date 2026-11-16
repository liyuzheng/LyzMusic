package yz.li.domain.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import yz.li.domain.database.AppDatabase
import yz.li.domain.database.dao.RemoteDao

/**
 * desc:
 * created by liyuzheng on 2026/4/7 18:28
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {

    @Provides
    fun providesRemote(
        database: AppDatabase,
    ): RemoteDao = database.remoteDao()
}
