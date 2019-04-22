/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.custom_views

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.settings_item.view.*
import ru.shadowsparky.screencast.R

class SettingsItem(context: Context, parent: LinearLayout) : LinearLayout(context) {
    private val view: View
    val mSettingName: TextView
    val mCurrentSetting: TextView
    private val mCard: CardView

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.settings_item, parent, false)
        this.mSettingName = view.findViewById(R.id.setting_name)
        this.mCurrentSetting = view.findViewById(R.id.current_setting)
        this.mCard = view.findViewById(ru.shadowsparky.screencast.R.id.setting_card)
        parent.addView(view)
    }

    companion object {
        fun generateNewSection(section_name: String, context: Context) : TextView {
            val result = TextView(context)
            result.text = section_name
            result.textSize = 16.0F
            result.setTypeface(null, Typeface.BOLD)
            val llp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            llp.setMargins(8, 8, 8, 8)
            result.layoutParams = llp
            return result
        }
    }
}