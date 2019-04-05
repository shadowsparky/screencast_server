/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.os.SystemClock
import android.view.*
import android.widget.LinearLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.shadowsparky.screencast.extras.*
import ru.shadowsparky.screencast.extras.Constants.Companion.ACTION
import ru.shadowsparky.screencast.extras.Constants.Companion.CONNECTION_CLOSED_CODE
import ru.shadowsparky.screencast.extras.Constants.Companion.CONNECTION_STARTED_CODE
import ru.shadowsparky.screencast.extras.Constants.Companion.DATA
import ru.shadowsparky.screencast.extras.Constants.Companion.DEFAULT_DPI
import ru.shadowsparky.screencast.extras.Constants.Companion.DEFAULT_HEIGHT
import ru.shadowsparky.screencast.extras.Constants.Companion.DEFAULT_NOTIFICATION_ID
import ru.shadowsparky.screencast.extras.Constants.Companion.DEFAULT_PORT
import ru.shadowsparky.screencast.extras.Constants.Companion.DEFAULT_PROJECTION_NAME
import ru.shadowsparky.screencast.extras.Constants.Companion.DEFAULT_WIDTH
import ru.shadowsparky.screencast.extras.Constants.Companion.NOTHING
import ru.shadowsparky.screencast.extras.Constants.Companion.REASON
import ru.shadowsparky.screencast.extras.Constants.Companion.RECEIVER_CODE
import ru.shadowsparky.screencast.extras.Constants.Companion.RECEIVER_DEFAULT_CODE
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.net.*


class ProjectionServer : Service(), View.OnTouchListener {
    private lateinit var mData: Intent
    private lateinit var mProjectionManager: MediaProjectionManager
    private val TAG = "ProjectionServer"
    private var mProjection: MediaProjection? = null
    private var mServerSocket: ServerSocket? = null
    private var mClientSocket: Socket? = null
    private var mClientStream: ObjectOutputStream? = null
    private var width = DEFAULT_WIDTH
    private var height = DEFAULT_HEIGHT
    private var mSurface: Surface? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mDisplay: Display? = null
    private var mCodec: MediaCodec? = null
    private var mFormat: MediaFormat? = null
    private var mCallback: MediaCodec.Callback? = null
    private val mSendingBuffers = Injection.provideByteQueue()
    private val log: Logger = Injection.provideLogger()
    private val mUtils: Utils = Injection.provideUtils()
    private var notification: Notification? = null
    private var reason = NOTHING
    private var broadcast: Intent? = null
    private var mClientDataStream: DataOutputStream? = null
    private var layout: LinearLayout? = null
    private var mWindowManager: WindowManager? = null

    private var handling: Boolean = false
        set(value) {
            if (value) {
                initBroadcast()
                broadcast!!.putExtra(RECEIVER_CODE, RECEIVER_DEFAULT_CODE)
                broadcast!!.putExtra(ACTION, CONNECTION_STARTED_CODE)
                sendBroadcast(broadcast!!)
                log.printDebug("Handling enabled")
            } else {
                initBroadcast()

                broadcast!!.putExtra(RECEIVER_CODE, RECEIVER_DEFAULT_CODE)
                broadcast!!.putExtra(ACTION, CONNECTION_CLOSED_CODE)
                broadcast!!.putExtra(REASON, reason)
                sendBroadcast(broadcast!!)
                reason = NOTHING
                log.printDebug("Handling disabled")
                socketSafeClosing()
                clientSocketSafeClosing()
                mCodec?.stop()
                this.stopSelf()
            }
            field = value
        }

    private fun initBroadcast() {
        broadcast = Intent(Constants.BROADCAST_ACTION)
    }
    private fun socketSafeClosing() {
        try {
            if (mServerSocket != null) {
                if (!mServerSocket!!.isClosed) {
                    mServerSocket!!.close()
                }
            }
        } catch (e: SocketException) {
            log.printDebug("Socket exception in Socket Safe Closing")
        }
    }

    private fun clientSocketSafeClosing() {
        try {
            if (mClientSocket != null) {
                if (!mClientSocket!!.isClosed) {
                    mClientSocket!!.close()
                }
            }
        } catch (e: SocketException) {
            log.printDebug("Socket exception in Socket Safe Closing")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        enableTouchEventHandler()
        mData = intent!!.getParcelableExtra(DATA)
        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        createNotification()
        startServer()
        return START_NOT_STICKY
    }

    private fun createNotification() {
        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification = Notifications(this).provideNotification(notificationService)
        startForeground(DEFAULT_NOTIFICATION_ID, notification)
    }

    private fun startServer() = GlobalScope.launch(Dispatchers.IO)  {
        try {
            mServerSocket = ServerSocket(DEFAULT_PORT)
        } catch (e: BindException) {
            reason = "Данный адрес уже используется"
            handling = false
            return@launch
        }
        mServerSocket!!.soTimeout = 30000
        log.printDebug("Waiting connection...", TAG)
        try {
            mClientSocket = mServerSocket!!.accept()
        } catch (e: SocketTimeoutException) {
            reason = "Превышено время ожидания подключения"
            handling = false
            log.printDebug("SocketTimeoutException")
            return@launch
        } catch (e: SocketException) {
            reason = "Соединение неожиданно прервалось"
            handling = false
            return@launch
        }
        log.printDebug("Connection accepted.", TAG)
        mClientStream = ObjectOutputStream(BufferedOutputStream(mClientSocket!!.getOutputStream()))
        mClientDataStream = DataOutputStream(BufferedOutputStream(mClientSocket!!.getOutputStream()))
        configureProjection()
        startProjection()
        sendProjectionData()
    }

    private fun sendProjectionInfo() {
        mClientStream!!.writeObject(PreparingData(width, height))
        mClientStream!!.flush()
    }

    private fun enableTouchEventHandler() {
        layout = LinearLayout(this)
        val lp = WindowManager.LayoutParams(30, WindowManager.LayoutParams.MATCH_PARENT)
        layout!!.layoutParams = lp
        //layout!!.setBackgroundColor(Color.CYAN)
        layout!!.setOnTouchListener(this)
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val mParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
         mParams.gravity = Gravity.TOP
        mWindowManager!!.addView(layout, mParams)
    }

    private fun touchDisplay(x: Float, y: Float) {
        val event = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis() + 100,
                MotionEvent.ACTION_UP,
                x,
                y,
                0
        )
        layout?.dispatchTouchEvent(event)
    }

    private fun sendProjectionData() = GlobalScope.launch(Dispatchers.IO) {
        try {
            sendProjectionInfo()
            log.printDebug("Data sending...", TAG)
            mClientSocket!!.tcpNoDelay = true
            handling = true
            while (handling) {
                val data = mSendingBuffers.take()
                mClientDataStream!!.writeInt(data.length)
                mClientDataStream!!.write(data.data)
            }
        } catch (e: InterruptedException) {
            log.printError("InterruptedException")
            handling = false
            return@launch
        } catch (e: IOException) {
            handling = false
            log.printError("IOException")
            return@launch
        }
    }.start()

    private fun configureProjection() {
        mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData)
        mDisplay = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        updateDisplayInfo()
        mFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        mFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, 12000000)
        mFormat!!.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mFormat!!.setFloat(MediaFormat.KEY_FRAME_RATE, /*mDisplay!!.refreshRate*/ (15).toFloat())
        mFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mCallback = ProjectionCallback(mSendingBuffers, mCodec!!)
        mCodec!!.setCallback(mCallback)
        mCodec!!.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mSurface = mCodec!!.createInputSurface()
    }

    private fun updateDisplayInfo() {
        val size = Point()
        mUtils.overrideGetSize(mDisplay!!, size)
        if (size.y > size.x) {
            width = size.y
            height = size.x
        } else {
            width = size.x
            height = size.y
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        log.printDebug("TOUCHED", TAG)
        return true
    }

    private fun startProjection() {
        mCodec!!.start()
        mVirtualDisplay = mProjection!!.createVirtualDisplay(DEFAULT_PROJECTION_NAME, width, height, DEFAULT_DPI, 0, mSurface, null, null)

    }
}
