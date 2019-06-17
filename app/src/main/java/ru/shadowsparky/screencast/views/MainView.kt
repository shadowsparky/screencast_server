/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.views

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_main.*
import ru.shadowsparky.screencast.ProjectionService
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.ServerBase
import ru.shadowsparky.screencast.ServerBase.ConnectionResult.*
import ru.shadowsparky.screencast.extras.Constants
import ru.shadowsparky.screencast.extras.Constants.BOUND
import ru.shadowsparky.screencast.extras.Constants.CAPREQUEST
import ru.shadowsparky.screencast.extras.Constants.LOADING
import ru.shadowsparky.screencast.extras.Constants.STATUS
import ru.shadowsparky.screencast.extras.Injection
import ru.shadowsparky.screencast.interfaces.Main
import ru.shadowsparky.screencast.interfaces.Actionable
import ru.shadowsparky.screencast.presenters.MainPresenter
import ru.shadowsparky.screencast.extras.Logger
import ru.shadowsparky.screencast.extras.NetworkListener

/**
 * View из MVP
 *
 * @property toast подробнее: [Toaster]
 * @property TAG тэг текущего класса
 * @property log подробнее: [Logger]
 * @property presenter подробнее: [Main.Presenter]
 * @property mService подробнее: [ProjectionService]
 * @property mCurrentStatus подробнее: [ConnectionStatus]
 * @property mBound статус текущей связки View и Service
 * @property mediaManager менеджер проецирования. позволяет запросить разрешение на проецирование.
 * @see [Fragment] [Actionable] [Main.View]
 * @since v1.0.0
 * @author shadowsparky
 */
class MainView : Fragment(), Actionable, Main.View {
    private lateinit var mediaManager : MediaProjectionManager
    private val TAG = "MainView"
    private val log = Injection.provideLogger()
    private val presenter: Main.Presenter = MainPresenter(this)
    lateinit var mService: ProjectionService; private set
    var mBound = false
    private val toast = Injection.provideToaster()
    var mCurrentStatus: ConnectionStatus = ConnectionStatus.NONE; private set
    private lateinit var networkListener: NetworkListener

    /**
     * Статус текущего соединения
     * @since v1.0.0
     * @author shadowsparky
     */
    enum class ConnectionStatus {
        /**
         * Не подключен
         * @since v1.0.0
         * @author shadowsparky
         */
        NONE,

        /**
         * Подключен
         * @since v1.0.0
         * @author shadowsparky
         */
        CONNECTED
    }

    override fun invoke(action: ServerBase.ConnectionResult) {
        status.text = when(action) {
            ADDRESS_ALREADY_IN_USE -> resources.getString(R.string.address_already_in_use)
            WAITING_FOR_CONNECTION -> resources.getString(R.string.waiting_for_connection)
            TIMEOUT -> resources.getString(R.string.timeout)
            UNEXPECTEDLY_DISCONNECTED -> resources.getString(R.string.unexpectedly_disconnected)
            ESTABLISHED -> resources.getString(R.string.established)
            BROKEN -> resources.getString(R.string.broken)
        }
        if ((action != WAITING_FOR_CONNECTION) and (action != ESTABLISHED)) reset()
    }


    override fun reset() {
        mService.close()
        status.text = ""
        setButtonStatus(ConnectionStatus.NONE)
    }

    override fun showToast(message: String) { toast.show(context!!, message) }

    override fun setService(service: ProjectionService) {
        mService = service
        mService.action = this
    }

    override fun setLoading(status: Boolean) = activity!!.runOnUiThread {
        capRequest?.isEnabled = !status
    }

    override fun setIPV4Text(text: String) = activity!!.runOnUiThread {
        ipv4.text = text
    }

    override fun setButtonStatus(status: ConnectionStatus) = activity!!.runOnUiThread {
        capRequest.text =
                if (status == ConnectionStatus.NONE)
                    resources.getString(R.string.launch)
                else
                    resources.getString(R.string.disconnect)
        mCurrentStatus = status
    }

    /**
     * Система вызывает этот метод при первом отображении пользовательского интерфейса фрагмента
     * на дисплее. Для прорисовки пользовательского интерфейса фрагмента следует возвратить из
     * этого метода объект [View], который является корневым в макете фрагмента.
     * Если фрагмент не имеет пользовательского интерфейса, можно возвратить null.
     *
     * @see [Fragment]
     * @since v1.0.0
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun bindService(connection: ServiceConnection) {
        log.printDebug("binding connection: $connection", TAG)
        val intent = Intent(context, ProjectionService::class.java)
        activity?.applicationContext?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun unbindService(connection: ServiceConnection) {
        mService.close()
        activity?.applicationContext?.unbindService(connection)
    }

    /**
     * Система вызывает этот метод, когда создает фрагмент.
     *
     * @see [Fragment]
     * @since v1.0.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaManager = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        networkListener = Injection.provideNetworkListener(activity!!)
        presenter.onFragmentCreated()
        networkListener.mAddr.observe(this, Observer {
            setIPV4Text(it)
        })
    }

    override fun sendCaptureRequest() = startActivityForResult(mediaManager.createScreenCaptureIntent(), Constants.REQUEST_CODE)

    /**
     * Вызывается, когда фрагмент виден пользователю. Обычно это связано с onStart () жизненного цикла, содержащего активность.
     *
     * @see [Fragment]
     * @since v1.0.0
     */
    override fun onStart() {
        super.onStart()
//        presenter.onFragmentLoaded()
        capRequest.setOnClickListener { presenter.onLaunchButtonClicked() }
    }

    /**
     * Вызывается, чтобы попросить фрагмент сохранить его текущее динамическое состояние,
     * чтобы впоследствии его можно было восстановить в новом экземпляре, процесс перезапускается.
     * Если позже потребуется создать новый экземпляр фрагмента, данные,
     * которые вы поместите в Bundle, будут доступны в Bundle, предоставленном [onViewStateRestored]
     *
     * @param Bundle: [Bundle] в котором разместится ваше сохраненное состояние.
     *
     * @see [Fragment]
     * @since v1.0.0
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        log.printDebug("On Save Instance State", TAG)
        outState.putBoolean(LOADING, !capRequest!!.isEnabled)
        outState.putBoolean(BOUND, mBound)
        outState.putString(STATUS, status.text.toString())
        outState.putString(CAPREQUEST, capRequest.text.toString())
    }

    /**
     * Вызывается, когда все сохраненное состояние восстанавливается в иерархии представлений фрагмента.
     * Это можно использовать для инициализации на основе сохраненного состояния, в котором вы позволяете иерархии
     * представления отслеживать себя, например, установлены ли флажки для виджетов в данный момент.
     *
     * @param Bundle: [Bundle] в котором размещается ваше сохраненное состояние.
     *
     * @see [Fragment]
     * @since v1.0.0
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        log.printDebug("On View State Restored", TAG)
        if (savedInstanceState != null) {
            setLoading(savedInstanceState.getBoolean(LOADING, false))
            mBound = savedInstanceState.getBoolean(BOUND, false)
            status.text = savedInstanceState.getString(STATUS)
            capRequest.text = savedInstanceState.getString(CAPREQUEST)
        }
    }

    override fun onResume() {
        super.onResume()
        networkListener.bindNetworkCallback()
    }

    override fun onPause() {
        super.onPause()
        networkListener.unbindNetworkCallback()
    }

    /**
     * Вызывается, когда фрагмент больше не используется.
     *
     * @see [Fragment]
     * @since v1.0.0
     */
    override fun onDestroy() {
        super.onDestroy()
        presenter.onFragmentDestroyed()
    }

    /**
     * Вызывается, когда пользователь отвечает на разрешение о захвате экрана
     *
     * @param requestCode код реквеста
     * @param resultCode код результата
     * @param data пакет с полученными данными
     * @see [Fragment]
     * @since v1.0.0
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }
}