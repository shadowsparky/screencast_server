/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.interfaces

/**
 * Обратный вызов передачи изображения
 *
 * @since v1.0.0
 * @author shadowsparky
 */
interface Sendeable {
    /**
     * Обратный вызов отправки изображения
     *
     * @param picture Изображение в формате [ByteArray]
     * @since v1.0.0
     * @author shadowsparky
     */
    fun sendPicture(picture: ByteArray)

    /**
     * Обратный вызов отправки данных о изображении
     *
     * @since v1.0.0
     * @author shadowsparky
     */
    fun sendPreparingData()
}