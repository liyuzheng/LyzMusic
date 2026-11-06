package yz.li.music

import android.app.Application
import timber.log.Timber

class LyzMusicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}