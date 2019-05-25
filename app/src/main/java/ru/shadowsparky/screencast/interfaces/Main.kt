/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.interfaces

import android.content.Intent
import android.content.ServiceConnection
import kotlinx.coroutines.Job
import ru.shadowsparky.screencast.ProjectionService
import ru.shadowsparky.screencast.presenters.MainPresenter
import ru.shadowsparky.screencast.views.MainFragment
import ru.shadowsparky.screencast.models.MainModel

/**
 * Интерфейс для MVP. Здесь находится логика Model View Presenter для классов с префиксом Main*
 *
 * @since v1.0.0
 * @author shadowsparky
 */
interface Main {

    /**
     * Методы представления
     *
     * @sample [MainFragment]
     * @since v1.0.0
     * @author shadowsparky
     */
    interface View {
        /**
         * Сброс соединения
         *
         * @since v1.0.0
         * @author shadowsparky
         */
        fun reset()

        /**
         * Вывод Toast уведомления
         *
         * @since v1.0.0
         * @author shadowsparky
         */
        fun showToast(message: String)

        /**
         * Установление нового сервиса в View
         *
         * @since v1.0.0
         * @author shadowsparky
         */
        fun setService(service: ProjectionService)

        /**
         * Установление нового IPV4 адреса устройства в View
         *
         * @since v1.0.0
         * @author shadowsparky
         */
        fun setIPV4Text(text: String)

        /**
         * Установление статуса загрузки в View
         *
         * @since v1.0.0
         * @author shadowsparky
         */
        fun setLoading(status: Boolean)

        /**
         * Установление статуса кнопки
         *
         * @see [MainFragment.ConnectionStatus]
         * @since v1.0.0
         * @author shadowsparky
         */
        fun setButtonStatus(status: MainFragment.ConnectionStatus)

        /**
         * Привязка сервиса к View
         *
         * @param connection [ServiceConnection]
         * @since v1.0.0
         * @author shadowsparky
         */
        fun bindService(connection: ServiceConnection)

        /**
         * Отвязка сервиса у View
         *
         * @param connection [ServiceConnection]
         * @since v1.0.0
         * @author shadowsparky
         */
        fun unbindService(connection: ServiceConnection)

        /**
         * Отправка запроса на захват экрана
         *
         * @since v1.0.0
         * @author shadowsparky
         */
        fun sendCaptureRequest()
    }

    /**
     * Методы Presenter
     *
     * @sample MainPresenter
     * @since v1.0.0
     * @author shadowsparky
     */
    interface Presenter {

        /**
         * Вызывается 1 раз при создании фрагмента
         * Подробнее: вызов при запуске метода onCreate в View
         *
         * @since v1.0.0
         * @author shadowsparky
         */
        fun onFragmentCreated()

        /**
         * Вызывается каждый раз, когда фрагмент загружается
         * Подробнее: вызов при запуске метода onStart в View
         *
         * @since v1.0.0
         * @author shadowsparky
         */
        fun onFragmentLoaded()

        /**
         * Вызывается 1 раз при уничтожении фрагмента
         * Подробнее: вызов при запуске метода onDestroy в View
         * @since v1.0.0
         * @author shadowsparky
         */
        fun onFragmentDestroyed()

        /**
         * Вызывается при нажатии на кнопку запуска/отключения сервера
         *
         * @since v1.0.0
         * @author shadowsparky
         */
        fun onLaunchButtonClicked()

        /**
         * Вызывается, если пользователь дал доступ приложению к взятию изображения с экрана
         *
         * @param data пакет с полученными данными от [onActivityResult]
         * @return Job возвращает асинхронный запрос
         */
        fun onLaunchServiceRequest(data: Intent?) : Job

        /**
         * Вызывается, когда в View срабатывает onActivityResult
         *
         * @param requestCode код реквеста
         * @param resultCode код результата
         * @param data пакет с полученными данными от onActivityResult во View
         * @since v1.0.0
         * @author shadowsparky
         */
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    /**
     * Методы Model
     *
     * @sample MainModel
     * @since v1.0.0
     * @author shadowsparky
     */
    interface Model {
        /**
         * @return Возвращает IpV4 адрес устройства
         * @since v1.0.0
         * @author shadowsparky
         */
        fun getIpV4Request() : String

        /**
         * Запуск сервиса [ProjectionService] для взятия данных с экрана
         *
         * @param mService сервис [ProjectionService]
         * @param data пакет с полученными данными от onActivityResult во View
         * @return если сервер запускается без ошибок, то true. В противном случае false
         */
        suspend fun launchService(mService: ProjectionService, data: Intent) : Boolean
    }
}