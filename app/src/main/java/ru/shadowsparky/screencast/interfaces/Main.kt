/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.interfaces

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import kotlinx.coroutines.Job
import ru.shadowsparky.screencast.ProjectionService
import ru.shadowsparky.screencast.views.MainFragment

interface Main {
    interface View {
        fun reset()
        fun showToast(message: String)
        fun setService(service: ProjectionService)
        fun setIPV4Text(text: String)
        fun setLoading(status: Boolean)
        fun setButtonStatus(status: MainFragment.ConnectionStatus)
        fun bindService(connection: ServiceConnection)
        fun unbindService(connection: ServiceConnection)
        fun sendCaptureRequest()
    }
    interface Presenter {
        fun onFragmentCreated()
        fun onFragmentLoaded()
        fun onFragmentDestroyed()
        fun onLaunchButtonClicked()
        fun onLaunchServiceRequest(data: Intent?) : Job
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }
    interface Model {
        fun getIpV4Request() : String
        suspend fun launchService(mService: ProjectionService, data: Intent) : Boolean
    }
}