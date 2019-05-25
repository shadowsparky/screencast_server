/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import android.content.Intent
import android.content.res.Configuration
import android.media.MediaCodec
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext as async

/**
 * Сервис, используемый для проецирования
 *
 * @see [ServerBase]
 * @since v1.0.0
 * @author shadowsparky
 */
class ProjectionService : ServerBase() {
    override val TAG: String = "ProjectionService"

    /**
     * Запуск сервера
     *
     * @param mData дополнительная информация об выданных пользователем разрешениях. Необхдимо для [MediaCodec]
     * @return если сервер успешно запущен, то возвращается true. В противном случае false
     * @since v1.0.0
     * @author shadowsparky
     */
    suspend fun launch(mData: Intent) : Boolean = async (IO) {
        this@ProjectionService.mData = mData
        var result = async(IO) { createServer() }
        if (!result)
            return@async result
        result = async(IO) { accept() }
        if (!result)
            return@async result
        createNotification()
        setupProjection()
        start()
        return@async true
    }

    /**
     * Вызывается системой при изменении конфигурации устройства во время работы компонента.
     * Обратите внимание, что, в отличие от действий, другие компоненты никогда не перезапускаются
     * при изменении конфигурации: они всегда должны иметь дело с результатами изменения,
     * например путем повторного получения ресурсов. В то время, когда эта функция была вызвана,
     * ваш объект Resources будет обновлен для возврата значений ресурсов, соответствующих новой конфигурации.
     *
     * @param newConfig новая конфигурация устройства. Это значение никогда не должно быть null
     * @since v1.0.0
     * @author shadowsparky
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (handling) {
            GlobalScope.launch(IO) {
                log.printDebug("Configuration Changed")
                stop()
                setupProjection()
                start()
            }
        }
    }

    /**
     * Создание нового экземпляра привязанной службы
     * @see [Binder], [IBinder]
     * @since v1.0.0
     * @author shadowsparky
     */
    private val mBinder = ProjectionBinder()

    /**
     * @return возвращает канал связи
     * @see [IBinder]
     * @since v1.0.0
     * @author shadowsparky
     */
    override fun onBind(intent: Intent?): IBinder = mBinder

    /**
     * Привязанная служба
     * @see [Binder]
     * @since v1.0.0
     * @author shadowsparky
     */
    inner class ProjectionBinder : Binder() {
        fun getService() = this@ProjectionService
    }
}