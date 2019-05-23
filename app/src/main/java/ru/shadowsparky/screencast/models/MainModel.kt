/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.models

import android.content.Intent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import ru.shadowsparky.screencast.ProjectionService
import ru.shadowsparky.screencast.extras.Injection
import ru.shadowsparky.screencast.interfaces.Main

class MainModel : Main.Model {
    override fun getIpV4Request() = Injection.provideIpHandler().getIpv4()
    override suspend fun launchService(mService: ProjectionService, data: Intent) = withContext(IO) {
        mService.launch(data)
    }
}