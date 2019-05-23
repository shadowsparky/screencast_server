/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.interfaces

import ru.shadowsparky.screencast.ServerBase

interface Actionable {
    fun invoke(action: ServerBase.ConnectionResult)
}