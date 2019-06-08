/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.extras

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.Network
import android.os.Build
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Представляет собой обработчик событий, связанных с сетью
 *
 * @param context Контекст
 * @property mAddr текущий ipv4 адрес
 * @property handler подробнее: [IpHandler]
 * @property manager менеджер событий свзязанных с сетью
 * @property networkChangeReceiver обработчик для устройств с версей Android M
 * @property networkChangeListener обработчик для устройств с Android N и выше
 */
class NetworkListener(private val context: Context) {
    val mAddr = MutableLiveData<String>()
    private val handler = Injection.provideIpHandler()
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mAddr.value = handler.getIpv4()
        }
    }

    private val networkChangeListener = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            super.onAvailable(network)
            GlobalScope.launch(Dispatchers.Main){
                mAddr.value = handler.getIpv4()
            }
        }
    }

    /**
     * Создает привязку
     */
    fun bindNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager.registerDefaultNetworkCallback(networkChangeListener)
        } else {
            val intentFilter = IntentFilter()
            intentFilter.addAction(CONNECTIVITY_ACTION)
            context.registerReceiver(networkChangeReceiver, intentFilter)
        }
    }

    /**
     * Отвязывается от событий
     */
    fun unbindNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager.unregisterNetworkCallback(networkChangeListener)
        } else {
            context.unregisterReceiver(networkChangeReceiver)
        }
    }
}