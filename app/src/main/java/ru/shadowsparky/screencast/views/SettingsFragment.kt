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
import ru.shadowsparky.screencast.extras.SettingsParser
import ru.shadowsparky.screencast.extras.SharedUtils
import ru.shadowsparky.screencast.interfaces.ChangeSettingsHandler
import ru.shadowsparky.screencast.interfaces.Settingeable

class SettingsFragment : Fragment(), Settingeable, ChangeSettingsHandler {
    private val toast = Injection.provideToaster()
    private val quality_list = listOf("100%", "75%", "50%", "30%")
    private val framerate_list = listOf("60", "45", "30", "15", "5")
    private val waiting_list = listOf("5 секунд", "15 секунд", "30 секунд", "60 секунд")
    private lateinit var shared: SharedUtils

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private fun attachSetting(choose: SettingsChoose) {
        val first = SettingsItem(context!!, settings_layout, choose, this)
        first.mSettingName.text = SettingsParser.getSectionName(choose)
        if (choose == SettingsChoose.PASSWORD) {
            val exists = shared.read(choose.name) != ""
            first.mCurrentSetting.text = if (exists) "Существует" else "Отсутствует"
        } else  {
            first.mCurrentSetting.text = shared.read(choose.name)
        }
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
            SettingsChoose.WAITING -> {
                val dialog = ChooseDialog(context!!, waiting_list, this, choose)
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
        attachSetting(SettingsChoose.IMAGE_QUALITY)
        attachSetting(SettingsChoose.FRAMERATE)
        settings_layout.addView(SettingsItem.generateNewSection("Защита", context!!))
        attachSetting(SettingsChoose.PASSWORD)
        settings_layout.addView(SettingsItem.generateNewSection("Остальное", context!!))
        attachSetting(SettingsChoose.WAITING)
        settings_layout.addView(SettingsItem.generateCopyright("AVB Cast.\nCreated By Shadowsparky, in 2019", context!!))
    }
}
