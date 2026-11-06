package yz.li.music.platformlog

import com.google.auto.service.AutoService
import timber.log.Timber

@AutoService(ILogPlatformAction::class)
class SamsungLogAction : ILogPlatformAction {
    override fun log() {
        Timber.v("log from samsung")
    }
}