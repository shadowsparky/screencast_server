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
import ru.shadowsparky.screencast.custom_views.SettingsItem

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onStart() {
        super.onStart()
        settings_layout.addView(SettingsItem.generateNewSection("Настройка изображения", context!!))
        val first = SettingsItem(context!!, settings_layout)
        first.mSettingName.text = "test"
        first.mCurrentSetting.text = "current_setting"
    }
}
