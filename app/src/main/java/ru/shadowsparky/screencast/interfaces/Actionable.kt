/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.interfaces

import ru.shadowsparky.screencast.ServerBase
import ru.shadowsparky.screencast.views.MainView

/**
 * Обратный вызов [ServerBase.ConnectionResult]
 *
 * @sample [MainView]
 * @since v1.0.0
 * @author shadowsparky
 */
interface Actionable {
    /**
     * вызов метода callback'a
     * @param action действие
     * @since v1.0.0
     * @author shadowsparky
     */
    fun invoke(action: ServerBase.ConnectionResult)
}