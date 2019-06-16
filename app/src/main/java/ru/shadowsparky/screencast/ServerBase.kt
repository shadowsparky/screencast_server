/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import android.app.*
import android.app.Service.START_NOT_STICKY
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.google.protobuf.ByteString
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import ru.shadowsparky.screencast.extras.*
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_DPI
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_HEIGHT
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_NOTIFICATION_ID
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_PORT
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_WIDTH
import ru.shadowsparky.screencast.extras.Constants.DISMISS
import ru.shadowsparky.screencast.interfaces.Actionable
import ru.shadowsparky.screencast.interfaces.Sendeable
import ru.shadowsparky.screencast.proto.HandledPictureOuterClass
import ru.shadowsparky.screencast.proto.PreparingDataOuterClass
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.OutputStream
import java.net.*

/**
 * Базовый класс для сервера
 *
 * @see [Service], [Sendeable], [Closeable]
 * @author shadowsparky
 * @since v1.0.0
 */
abstract class ServerBase : Service(), Sendeable, Closeable {

    /**
     * Перечисление вариантов результата соединения
     *
     * @author shadowsparky
     * @since v1.0.0
     */
    enum class ConnectionResult {
        /**
         * Адрес уже используется
         *
         * @author shadowsparky
         * @since v1.0.0
         */
        ADDRESS_ALREADY_IN_USE,

        /**
         * Ожидание подключения
         *
         * @author shadowsparky
         * @since v1.0.0
         */
        WAITING_FOR_CONNECTION,

        /**
         * Превышено время ожидания
         *
         * @author shadowsparky
         * @since v1.0.0
         */
        TIMEOUT,

        /**
         * Соединение нежиданно прервалось
         *
         * @author shadowsparky
         * @since v1.0.0
         */
        UNEXPECTEDLY_DISCONNECTED,

        /**
         * Подключение установлено
         *
         * @author shadowsparky
         * @since v1.0.0
         */
        ESTABLISHED,

        /**
         * Соединение разорвано
         *
         * @author shadowsparky
         * @since v1.0.0
         */
        BROKEN
    }

    /**
     * Тэг текущего класса
     * @author shadowsparky
     * @since v1.0.0
     */
    protected abstract val TAG: String

    /**
     * Дополнительная информация об выданных пользователем разрешениях. Необхдимо для [MediaCodec]
     * @see [Intent]
     * @since v1.0.0
     */
    protected lateinit var mData: Intent

    /**
     * Управляет поиском определенных типов токенов [MediaProjection]
     * @see [MediaProjectionManager]
     * @since v1.0.0
     */
    private lateinit var mProjectionManager: MediaProjectionManager

    /**
     * Токен, дающий приложениям возможность захватывать содержимое экрана и / или записывать системное аудио.
     * Точные предоставляемые возможности зависят от типа [MediaProjection]
     * @see [MediaProjection]
     * @since v1.0.0
     */
    private var mProjection: MediaProjection? = null

    /**
     * Этот класс реализует сокеты сервера. Серверный сокет ожидает поступления запросов по сети.
     * Он выполняет некоторую операцию на основе этого запроса, а затем, возможно, возвращает результат запрашивающей стороне.
     * @see [ServerSocket]
     * @since v1.0.0
     */
    private var mServer: ServerSocket? = null

    /**
     * Этот класс реализует клиентские сокеты (также называемые просто «сокетами»).
     * Сокет является конечной точкой для связи между двумя машинами.
     * @see Socket
     * @since v1.0.0
     */
    private var mClient: Socket? = null

    /**
     * Переменная для сохранения ширины дисплея. По умолчанию: [DEFAULT_WIDTH]
     * @since v1.0.0
     */
    private var width = DEFAULT_WIDTH

    /**
     * Переменная для сохранения высоты дисплея. По умолчанию: [DEFAULT_HEIGHT]
     * @since v1.0.0
     */
    private var height = DEFAULT_HEIGHT

    /**
     * Обработка необработанного буфера, который управляется композитором экрана.
     * @see Surface
     * @since v1.0.0
     */
    private var mSurface: Surface? = null

    /**
     * Представляет виртуальный дисплей.
     * Содержимое виртуального дисплея отображается на [Surface], который вы должны предоставить [DisplayManager.createVirtualDisplay]
     * @see [VirtualDisplay]
     * @since v1.0.0
     */
    private var mVirtualDisplay: VirtualDisplay? = null

    /**
     * Предоставляет информацию о размере и плотности логического дисплея.
     * @see [Display]
     * @since v1.0.0
     */
    private var mDisplay: Display? = null

    /**
     * [MediaCodec] используется для доступа к низкоуровневым кодекам.
     * В данном случае используется компонент кодирования изображения
     * @see [MediaCodec]
     * @since v1.0.0
     */
    private var mCodec: MediaCodec? = null

    /**
     * Инкапсулирует информацию, описывающую формат медиа-данныx
     *
     * @see [MediaFormat]
     * @since v1.0.0
     */
    private var mFormat: MediaFormat? = null

    /**
     * @see [ProjectionCallback]
     * @since v1.0.0
     * @author shadowsparky
     */
    private var mCallback: ProjectionCallback? = null

    /**
     * @see [Logger]
     * @since v1.0.0
     * @author shadowsparky
     */
    protected val log: Logger = Injection.provideLogger()

    /**
     * @see [DisplayUtils]
     * @since v1.0.0
     * @author shadowsparky
     */
    private val mUtils: DisplayUtils = Injection.provideUtils()

    /**
     * @see [Notification]
     * @since v1.0.0
     * @author shadowsparky
     */
    private lateinit var mNotification: Notification

    /**
     * @see [SharedUtils]
     * @since v1.0.0
     * @author shadowsparky
     */
    private lateinit var mShared: SharedUtils

    /**
     * @see [SettingsParser]
     * @since v1.0.0
     * @author shadowsparky
     */
    private lateinit var mSettingsParser: SettingsParser

    /**
     * [HandlerThread] используемый для асинхронной реализации [MediaCodec]
     * @see [HandlerThread]
     * @since v1.0.0
     */
    private var mEncoderThread = HandlerThread("EncoderThread", Process.THREAD_PRIORITY_URGENT_DISPLAY)

    /**
     * @see [Actionable]
     * @since v1.0.0
     * @author shadowsparky
     */
    var action: Actionable? = null

    /**
     * Флаг соединения с клиентом
     * Если true, то соединение установлено, иначе false
     * @since v1.0.0
     * @author shadowsparky
     */
    var handling: Boolean = false

    /**
     * Обратный вызов нажатия кнопки "Отключиться" на уведомлении
     * @since v1.0.0
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            this@ServerBase.action?.invoke(ConnectionResult.UNEXPECTEDLY_DISCONNECTED)
        }
    }

    private var stream: OutputStream? = null

    /**
     * Вызывается системой каждый раз, когда клиент явно запускает службу, вызывая Context.startService (Intent),
     * предоставляя предоставленные им аргументы и уникальный целочисленный токен, представляющий запрос на запуск.
     *
     * @return [START_NOT_STICKY] сервис не будет перезапущен после того, как был убит системой
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    /**
     * Система вызывает этот метод, когда создает сервис.
     *
     * @see [Service]
     * @since v1.0.0
     */
    override fun onCreate() {
        super.onCreate()
        log.printDebug("Sever Base Created", TAG)
        mShared = Injection.provideSharedUtils(baseContext)
        mSettingsParser = Injection.provideSettingsParser(baseContext)
        mProjectionManager = baseContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        registerReceiver(receiver, IntentFilter(DISMISS))
        mEncoderThread.start()
    }

    /**
     * Система вызывает этот метод, когда уничтожает сервис.
     *
     * @see [Service]
     * @since v1.0.0
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    /**
     * Создание сервера
     * Если сервер создался, в [Actionable] срабатывает [Actionable.invoke] с параметром [ConnectionResult.WAITING_FOR_CONNECTION],
     * иначе с параметром [ConnectionResult.ADDRESS_ALREADY_IN_USE]
     *
     * @author shadowsparky
     * @since v1.0.0
     * @return true если сервер создался, иначе false
     */
    open fun createServer() : Boolean {
        try {
            mServer = ServerSocket(DEFAULT_PORT)
            mServer?.soTimeout = mSettingsParser.getWaiting()

        } catch (e: BindException) {
            action?.invoke(ConnectionResult.ADDRESS_ALREADY_IN_USE)
            return false
        }
        action?.invoke(ConnectionResult.WAITING_FOR_CONNECTION)
        return true
    }

    /**
     * Срабатывает запрос на принятие соединения.
     * Если время ожидания превышает заданное, то срабатывает [Actionable.invoke] с параметром [ConnectionResult.TIMEOUT]
     * Если соединение неожиданно закроется, то сработает [Actionable.invoke] с параметром [ConnectionResult.UNEXPECTEDLY_DISCONNECTED]
     * Если принятие соединения произойдет, то сработает [Actionable.invoke] с параметром [ConnectionResult.ESTABLISHED]
     *
     * @author shadowsparky
     * @since v1.0.0
     * @return true если никаких ошибок не произошло, иначе false
     */
    open fun accept() : Boolean {
        try {
            mClient = mServer?.accept()
            mClient?.tcpNoDelay = true
            handling = true
//            mClient?.sendBufferSize = 50000
//            mClient?.receiveBufferSize = 50000
//            mServer?.receiveBufferSize = 50000
            stream = mClient?.getOutputStream()
        } catch (e: SocketTimeoutException) {
            action?.invoke(ConnectionResult.TIMEOUT)
            return false
        } catch (e: SocketException) {
            action?.invoke(ConnectionResult.UNEXPECTEDLY_DISCONNECTED)
            return false
        }
        action?.invoke(ConnectionResult.ESTABLISHED)
        return true
    }

    /**
     * Создание уведомления
     *
     * @author shadowsparky
     * @since v1.0.0
     * @see [Notifications]
     * @return возвращает созданное уведомление
     */
    protected open fun createNotification() : Notification {
        val dismissIntent = Intent(DISMISS)
        val dismissPI = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notificationService = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotification = Notifications(baseContext, dismissPI).provideNotification(notificationService)
        startForeground(DEFAULT_NOTIFICATION_ID, mNotification)
        return mNotification
    }

    /**
     * Обновление width и height. Метод срабатывает при перевороте устройства
     *
     * @see [DisplayUtils]
     * @author shadowsparky
     * @since v1.0.0
     */
    protected open fun updateDisplayInfo() {
        val size = Point()
        mUtils.overrideGetSize(mDisplay!!, size)
        width = size.x
        height = size.y
    }

    /**
     * Конфигурирование параметров [MediaCodec]
     *
     * @see [MediaCodec], [MediaFormat]
     * @author shadowsparky
     * @since v1.0.0
     */
    protected open fun configureMediaFormat() {
        mFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_VP8, width, height)
        mFormat!!.setInteger(MediaFormat.KEY_BIT_RATE,mSettingsParser.getBitrate())
        mFormat!!.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mFormat!!.setFloat(MediaFormat.KEY_FRAME_RATE, mSettingsParser.getFramerate().toFloat())
        mFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }

    /**
     * Конфигурирование [MediaCodec]
     *
     * @see [MediaCodec]
     * @author shadowsparky
     * @since v1.0.0
     */
    protected open fun configureMediaCodec() {
        mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_VP8)
        mCallback = ProjectionCallback(this, mCodec!!)
        mCodec!!.setCallback(mCallback, Handler(mEncoderThread.looper))
        mCodec!!.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    /**
     * Настройка проецирования
     *
     * @see [MediaProjection], [Display], [MediaCodec], [MediaFormat], [configureMediaCodec],
     * [configureMediaFormat], [sendPreparingData], [updateDisplayInfo], [Surface]
     * @author shadowsparky
     * @since v1.0.0
     */
    open fun setupProjection() {
        mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData)
        mDisplay = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        updateDisplayInfo()
        sendPreparingData()
        configureMediaFormat()
        configureMediaCodec()
        mSurface = mCodec!!.createInputSurface()
    }

    /**
     * Запуск проецирования
     *
     * @see [MediaCodec], [ProjectionCallback], [VirtualDisplay]
     * @author shadowsparky
     * @since v1.0.0
     */
    open fun start() {
        mCodec!!.start()
        val metrics = resources.displayMetrics
        val densityDpi = (metrics.density * 160f).toInt()
        mCallback!!.handling = true
        mVirtualDisplay = mProjection!!.createVirtualDisplay(getString(R.string.projection_name), width, height, densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, Handler(mEncoderThread.looper))
    }

    /**
     * Очистка всех предыдущих данных проецирования
     * Необходимо при повороте устройства, чтобы [MediaCodec] повторно сконфигурировался с новыми параметрами дисплея
     *
     * @author shadowsparky
     * @since v1.0.0
     */
    open fun stop() = release()

    /**
     * @see [stop]
     */
    open fun release() {
        mCodec?.release()
        mVirtualDisplay?.release()
        mSurface?.release()
        mCodec?.release()
        mProjection?.stop()
        mCallback?.handling = false
    }

    /**
     * Закрытие соединения сервера
     *
     * @author shadowsparky
     * @since v1.0.0
     */
    override fun close() {
        log.printDebug("Close invoke")
        if (mServer?.isClosed == false)
            mServer?.close()
        if (mClient?.isClosed == false)
            mClient?.close()
        release()
        stopForeground(true)
        handling = false
    }

    /**
     * Отправка изображения клиенту
     * Если изображение невозможно отправить (соединение завершилось), то срабатывает [Actionable.invoke] с параметром [ConnectionResult.BROKEN]
     *
     * @see [Sendeable]
     * @author shadowsparky
     * @since v1.0.0
     */
    override fun sendPicture(picture: ByteArray) {
        try {
            val item = HandledPictureOuterClass.HandledPicture.newBuilder()
                    .setEncodedPicture(ByteString.copyFrom(picture))
                    .build()
            item.writeDelimitedTo(stream)
        } catch (e: SocketException) {
            action?.invoke(ConnectionResult.BROKEN)
        }
    }

    /**
     * Отправка данных об изображении
     * Если данные невозможно отправить (соединение завершилось), то срабатывает [Actionable.invoke] с параметром [ConnectionResult.BROKEN]
     *
     * @see [Sendeable]
     * @author shadowsparky
     * @since v1.0.0
     */
    override fun sendPreparingData() {
        try {
            val data = PreparingDataOuterClass.PreparingData.newBuilder()
                    .setWidth(width)
                    .setHeight(height)
                    .setPassword("")
                    .build()
            data.writeDelimitedTo(stream)
        } catch (e: SocketException) {
            action?.invoke(ConnectionResult.BROKEN)
        }
    }
}