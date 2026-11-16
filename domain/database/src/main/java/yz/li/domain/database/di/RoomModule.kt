package yz.li.domain.database.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import yz.li.domain.database.AppDatabase
import yz.li.domain.database.api.DatabaseTransactionRunner
import yz.li.domain.database.provider.RoomTransactionRunnerProvider
import javax.inject.Singleton

/**
 * desc:
 * created by liyuzheng on 2026/4/7 16:53
 */
@Module
@InstallIn(SingletonComponent::class)
// 这里使用了 ApplicationComponent，因此 NetworkModule 绑定到 Application 的生命周期。
object RoomModule {
    @Provides
    @Singleton
    fun provideAppDataBase(application: Application): AppDatabase {
        return Room
            .databaseBuilder(application, AppDatabase::class.java, "androidComposeDemo.db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration(true)
            .enableMultiInstanceInvalidation()
            .build()
    }

    @Provides
    @Singleton
    fun provideRoomTransactionRunner(appDataBase: AppDatabase): DatabaseTransactionRunner {
        return RoomTransactionRunnerProvider(appDataBase)
    }
}