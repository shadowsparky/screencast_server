/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast

import android.content.Context
import android.content.Intent
import ru.shadowsparky.screencast.extras.Constants.Companion.DATA
import ru.shadowsparky.screencast.extras.Constants.Companion.VIEW

class MainPresenter : Main.Presenter {
    var view: Main.View? = null

    override fun attachView(view: Main.View) {
        this.view = view
    }

    override fun projectionRequest(data: Intent, context: Context) {
        val server = Intent(context, ProjectionServer::class.java)
        server.putExtra(DATA, data)
//        server.putExtra(VIEW, view!!)
        view!!.startServer(server)
    }
}