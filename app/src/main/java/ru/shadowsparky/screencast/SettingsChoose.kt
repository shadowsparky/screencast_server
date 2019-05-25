/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

/**
 * Перечисление настроек
 *
 * @since v1.0.0
 * @author shadowsparky
 */
enum class SettingsChoose {
    /**
     * Качество изображения (битрейт)
     *
     * @since v1.0.0
     * @author shadowsparky
     */
    IMAGE_QUALITY,

    @Deprecated("Расширение нигде не используется")
    /**
     * Расширение экрана
     *
     * @since v1.0.0
     * @author shadowsparky
     */
    EXPANSION,

    /**
     * Количество кадров в секунду (ФПС)
     *
     * @since v1.0.0
     * @author shadowsparky
     */
    FRAMERATE,

    @Deprecated("Пароль нигде не используется")
    /**
     * Пароль
     *
     * @since v1.0.0
     * @author shadowsparky
     */
    PASSWORD,

    /**
     * Время ожидания
     *
     * @since v1.0.0
     * @author shadowsparky
     */
    WAITING
}