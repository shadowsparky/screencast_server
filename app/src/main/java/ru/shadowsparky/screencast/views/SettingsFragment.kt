/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.views

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.shadowsparky.screencast.App
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.custom_views.SettingsItem
import ru.shadowsparky.screencast.dialogs.ChooseDialog
import ru.shadowsparky.screencast.extras.*
import ru.shadowsparky.screencast.interfaces.ChangeSettingsHandler
import ru.shadowsparky.screencast.interfaces.Settingeable
import ru.shadowsparky.screencast.views.SettingsFragment.Companion.BITRATE
import ru.shadowsparky.screencast.views.SettingsFragment.Companion.framerate_list
import ru.shadowsparky.screencast.views.SettingsFragment.Companion.quality_list
import ru.shadowsparky.screencast.views.SettingsFragment.Companion.waiting_list
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/**
 * Фрагмент, используемый в настройках приложения
 *
 * @property toast подробнее: [Toaster]
 * @property shared подробнее: [SharedUtils]
 * @property BITRATE список возможного битрейта
 * @property quality_list список возможного качества
 * @property framerate_list список возможного количества кадров в секунду
 * @property waiting_list список возможных значений для ожидания
 * @property displayUtils подробнее: [DisplayUtils]
 * @see [Fragment]
 * @see [Settingeable]
 * @see [ChangeSettingsHandler]
 * @author shadowsparky
 * @since v1.0.0
 */
class SettingsFragment : Fragment(), Settingeable, ChangeSettingsHandler {
    private val toast = Injection.provideToaster()
    private val displayUtils = Injection.provideUtils()

    companion object {
        var BITRATE = ArrayList<Int>(); private set
        var quality_list = ArrayList<String>(); private set
        var framerate_list = ArrayList<String>(); private set
        var waiting_list = ArrayList<String>(); private set

        fun initialize(context: Context) {
            BITRATE = arrayListOf(64, 128, 256, 512, 1, 3, 6, 10, -1)
            quality_list = arrayListOf (
                "${BITRATE[0]} ${context.getString(R.string.kb)} (${context.getString(R.string.min_quality)})",
                "${BITRATE[1]} ${context.getString(R.string.kb)}",
                "${BITRATE[2]} ${context.getString(R.string.kb)}",
                "${BITRATE[3]} ${context.getString(R.string.kb)}",
                "${BITRATE[4]} ${context.getString(R.string.mb)}",
                "${BITRATE[5]} ${context.getString(R.string.mb)}",
                "${BITRATE[6]} ${context.getString(R.string.mb)}",
                "${BITRATE[7]} ${context.getString(R.string.mb)} (${context.getString(R.string.max_quality)})"
            )
            waiting_list = arrayListOf(
                    "5 ${context.getString(R.string.seconds)}",
                    "15 ${context.getString(R.string.seconds)}",
                    "30 ${context.getString(R.string.seconds)}",
                    "60 ${context.getString(R.string.seconds)}"
            )
        }
    }
    private lateinit var shared: SharedUtils

    /**
     * Система вызывает этот метод при первом отображении пользовательского интерфейса фрагмента
     * на дисплее. Для прорисовки пользовательского интерфейса фрагмента следует возвратить из
     * этого метода объект [View], который является корневым в макете фрагмента.
     * Если фрагмент не имеет пользовательского интерфейса, можно возвратить null.
     *
     * @see [Fragment]
     * @since v1.0.0
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_settings, container, false)

    /**
     * Вставка настройки в фрагмент
     *
     * @author shadowsparky
     * @since v1.0.0
     */
    private fun attachSetting(choose: SettingsChoose) {
        val first = SettingsItem(context!!, settings_layout, choose, this)
        first.mSettingName.text = SettingsParser.getSectionName(choose, context!!)
        first.mCurrentSetting.text = shared.read(choose.name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onSettingsChanged(choose: SettingsChoose, value: String) {
        shared.write(choose.name, value)
        loadSetting()
    }

    override fun onSettingChoosed(choose: SettingsChoose) {
        val dialog = when (choose) {
            SettingsChoose.IMAGE_QUALITY -> ChooseDialog(context!!, quality_list, this, choose)
            SettingsChoose.FRAMERATE -> ChooseDialog(context!!, framerate_list, this, choose)
            SettingsChoose.WAITING -> ChooseDialog(context!!, waiting_list, this, choose)
            else -> throw RuntimeException("Unrecognized Choosing")
        }
        dialog.show()
    }

    /**
     * Инициализация списка возможного количества кадров в секунду
     *
     * @author shadowsparky
     * @since v1.0.0
     */
    fun initFramerate() {
        framerate_list.clear()
        var framerate = displayUtils.getRefreshRating(context!!)
        while (framerate >= 1) {
            framerate_list.add("${framerate.roundToInt()}")
            framerate -= 5
        }
    }

    /**
     * Вызывается, когда фрагмент виден пользователю. Обычно это связано с onStart () жизненного цикла, содержащего активность.
     * Если вы переопределите этот метод, вы должны обратиться к реализации суперкласса.
     *
     * @see [Fragment]
     * @since v1.0.0
     */
    override fun onStart() {
        super.onStart()
        initialize(context!!)
        shared = Injection.provideSharedUtils(context!!)
        shared.initialize()
        initFramerate()
        loadSetting()
    }

    /**
     * Отрисовка настроек на фрагменте
     *
     * @author shadowsparky
     * @since v1.0.0
     */
    private fun loadSetting() {
        settings_layout.removeAllViews()
        settings_layout.addView(SettingsItem.generateNewSection(resources.getString(R.string.setting_picture), context!!))
        attachSetting(SettingsChoose.IMAGE_QUALITY)
        attachSetting(SettingsChoose.FRAMERATE)
        settings_layout.addView(SettingsItem.generateNewSection(resources.getString(R.string.other), context!!))
        attachSetting(SettingsChoose.WAITING)
        settings_layout.addView(SettingsItem.generateCopyright(resources.getString(R.string.app_version), context!!))
    }
}
