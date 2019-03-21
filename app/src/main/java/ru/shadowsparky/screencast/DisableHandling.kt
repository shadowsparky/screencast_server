/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import java.io.Serializable

data class DisableHandling(
    val action: String = "disable"
) : Serializable