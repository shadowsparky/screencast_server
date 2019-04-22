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
import ru.shadowsparky.screencast.extras.Injection
import ru.shadowsparky.screencast.interfaces.Settingeable

class SettingsFragment : Fragment(), Settingeable {
    private val toast = Injection.provideToaster()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private fun attachSetting(setting_name: String, setting_text: String, choose: SettingsChoose) {
        val first = SettingsItem(context!!, settings_layout, choose, this)
        first.mSettingName.text = setting_name
        first.mCurrentSetting.text = setting_text
    }

    override fun handle(choose: SettingsChoose) {
        toast.show(context!!, "Данный раздел: ${choose.name} находится в разработке.")
    }

    override fun onStart() {
        super.onStart()
        settings_layout.addView(SettingsItem.generateNewSection("Настройка изображения", context!!))
        attachSetting("Качество изображения", "100%", SettingsChoose.IMAGE_QUALITY)
        attachSetting("Расширение", "1920:1080", SettingsChoose.EXPANSION)
        attachSetting("Кадров в секунду", "60", SettingsChoose.FRAMERATE)
        settings_layout.addView(SettingsItem.generateNewSection("Защита", context!!))
        attachSetting("Пароль", "пароль отсутствует", SettingsChoose.PASSWORD)
        settings_layout.addView(SettingsItem.generateNewSection("Остальное", context!!))
        attachSetting("Задержка", "30 секунд", SettingsChoose.DELAY)
        settings_layout.addView(SettingsItem.generateCopyright("AVB Cast.\nCreated By Shadowsparky, in 2019", context!!))
    }
}
