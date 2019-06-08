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

/**
 * Модель из MVP
 *
 * @see [Main.Model]
 * @since v1.0.0
 * @author shadowsparky
 */
class MainModel : Main.Model {
    override suspend fun launchService(mService: ProjectionService, data: Intent) = withContext(IO) {
        mService.launch(data)
    }
}