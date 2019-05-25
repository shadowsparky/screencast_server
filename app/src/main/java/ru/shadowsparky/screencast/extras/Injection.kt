/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import android.content.Context
import android.content.SharedPreferences
import ru.shadowsparky.screencast.ProjectionService

/**
 * Статический объект, используемый для внедрения паттерна Dependency Injection
 *
 * Фреймворк Dagger 2 не использовался, потому что в нем нет смысла, так как приложение небольшое.
 *
 * @property logInstance singleton объект [Logger]
 * @since v1.0.0
 * @author shadowsparky
 */
object Injection {
    private var logInstance = Logger()

    /**
     * Инъекция класса [IpHandler]
     *
     * @return новый экземпляр объекта [IpHandler]
     * @since v1.0.0
     * @author shadowsparky
     */
    fun provideIpHandler() : IpHandler = IpHandler()

    /**
     * Инъекция класса [SharedUtils]
     *
     * @param context для инициализации объекта [SharedPreferences] необходимо наличие [Context].
     * Так как [SharedUtils] всего лишь обёртка для [SharedPreferences], то передача [Context] обязательна.
     *
     * @return новый экземпляр объекта [SharedUtils]
     * @since v1.0.0
     * @author shadowsparky
     */
    fun provideSharedUtils(context: Context) : SharedUtils = SharedUtils(context)

    /**
     * Инъекция класса [SettingsParser]
     *
     * @param context в классе [SettingsParser] используется [SharedUtils], поэтому передача [Context] обязательна.
     * Подробнее: [provideSharedUtils]
     *
     * @return новый экземпляр объекта [SettingsParser]
     * @since v1.0.0
     * @author shadowsparky
     */
    fun provideSettingsParser(context: Context) : SettingsParser = SettingsParser(context)

    /**
     * Инъекция объекта [Logger]
     *
     * @return singleton объекта [Logger]
     * @since v1.0.0
     * @author shadowsparky
     */
    fun provideLogger() = logInstance

    /**
     * Инъекция класса [Toaster]
     *
     * @return новый экземпляр объекта [Toaster]
     * @since v1.0.0
     * @author shadowsparky
     */
    fun provideToaster() : Toaster = Toaster()

    /**
     * Инъекция класса [DisplayUtils]
     *
     * @return новый экземпляр объекта [DisplayUtils]
     * @since v1.0.0
     * @author shadowsparky
     */
    fun provideUtils() : DisplayUtils = DisplayUtils()
}