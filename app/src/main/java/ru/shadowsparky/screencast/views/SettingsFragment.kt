/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_settings.*
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.custom_views.SettingsItem
import ru.shadowsparky.screencast.dialogs.ChooseDialog
import ru.shadowsparky.screencast.extras.Injection
import ru.shadowsparky.screencast.extras.SharedUtils
import ru.shadowsparky.screencast.interfaces.ChangeSettingsHandler
import ru.shadowsparky.screencast.interfaces.Settingeable

class SettingsFragment : Fragment(), Settingeable, ChangeSettingsHandler {
    private val toast = Injection.provideToaster()
    private val quality_list = listOf("100%", "75%", "50%", "30%")
    private val framerate_list = listOf("60", "45", "30", "15", "5")
    private val delay_list = listOf("5 секунд", "15 секунд", "30 секунд", "60 секунд")
    private lateinit var shared: SharedUtils

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private fun attachSetting(setting_name: String, setting_text: String, choose: SettingsChoose) {
        val first = SettingsItem(context!!, settings_layout, choose, this)
        first.mSettingName.text = setting_name
        first.mCurrentSetting.text = setting_text
    }

    override fun onSettingsChanged(choose: SettingsChoose, value: String) {
        shared.write(choose.name, value)
        loadSetting()
    }

    override fun onSettingChoosed(choose: SettingsChoose) {
        when (choose) {
            SettingsChoose.IMAGE_QUALITY -> {
                val dialog = ChooseDialog(context!!, quality_list, this, choose)
                dialog.show()
            }
            SettingsChoose.FRAMERATE -> {
                val dialog = ChooseDialog(context!!, framerate_list, this, choose)
                dialog.show()
            }
            SettingsChoose.DELAY -> {
                val dialog = ChooseDialog(context!!, delay_list, this, choose)
                dialog.show()
            }
            else -> toast.show(context!!, "Данный раздел: ${choose.name} находится в разработке.")
        }
    }

    override fun onStart() {
        super.onStart()
        shared = Injection.provideSharedUtils(context!!)
        shared.initialize()
        loadSetting()
    }


    fun loadSetting() {
        settings_layout.removeAllViews()
        settings_layout.addView(SettingsItem.generateNewSection("Настройка изображения", context!!))
        loadQuality()
//        loadExpansion()
        loadFramerate()
        settings_layout.addView(SettingsItem.generateNewSection("Защита", context!!))
        loadPassword()
        settings_layout.addView(SettingsItem.generateNewSection("Остальное", context!!))
        loadDelay()
        settings_layout.addView(SettingsItem.generateCopyright("AVB Cast.\nCreated By Shadowsparky, in 2019", context!!))
    }

    fun loadQuality() {
        val current_setting = shared.read(SettingsChoose.IMAGE_QUALITY.name)
        attachSetting("Качество изображения", current_setting, SettingsChoose.IMAGE_QUALITY)
    }

    fun loadExpansion() {
        val current_setting = shared.read(SettingsChoose.EXPANSION.name)
        attachSetting("Расширение", current_setting, SettingsChoose.EXPANSION)
    }

    fun loadFramerate() {
        val current_setting = shared.read(SettingsChoose.FRAMERATE.name)
        attachSetting("Кадров в секунду", "$current_setting", SettingsChoose.FRAMERATE)
    }

    fun loadPassword() {
        val current_setting = if (shared.read(SettingsChoose.PASSWORD.name) == "")
            "пароль отсутствует"
        else
            "пароль присутствует"
        attachSetting("Пароль", current_setting, SettingsChoose.PASSWORD)
    }

    fun loadDelay() {
        val current_setting = shared.read(SettingsChoose.DELAY.name)
        attachSetting("Задержка", current_setting, SettingsChoose.DELAY)
    }

}
