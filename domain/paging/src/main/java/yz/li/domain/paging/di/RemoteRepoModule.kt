package yz.li.domain.paging.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import yz.li.domain.database.api.RemoteRepoApi
import yz.li.domain.database.dao.RemoteDao
import yz.li.domain.paging.repo.RemoteRepo

/**
 * desc:
 * created by liyuzheng on 2026/4/9 17:38
 */
@Module
@InstallIn(SingletonComponent::class)
class RemoteRepoModule {
    @Provides
    fun providesRemoteRepo(
        remoteDao: RemoteDao,
    ): RemoteRepoApi = RemoteRepo(remoteDao)
}