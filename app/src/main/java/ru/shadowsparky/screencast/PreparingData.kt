/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import java.io.Serializable

data class PreparingData(
        val width: Int,
        val height: Int,
        val key: String = "key"
) : Serializable