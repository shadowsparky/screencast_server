/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.interfaces

interface Sendeable {
    fun sendPicture(picture: ByteArray)
    fun sendPreparingData()
}