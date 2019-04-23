/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.custom_views

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.extras.Injection
import ru.shadowsparky.screencast.interfaces.Settingeable

class SettingsItem(context: Context, parent: LinearLayout, choosed_item: SettingsChoose, handler: Settingeable) : LinearLayout(context) {
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
        this.mCard.setOnClickListener {
            handler.onSettingChoosed(choosed_item)
        }
        parent.addView(view)
    }

    companion object {
        fun generateNewSection(section_name: String, context: Context) : TextView {
            val result = TextView(context)
            result.text = section_name
            result.setTextColor(Color.parseColor("#ff0099cc"))
            result.textSize = 16.0F
            result.setTypeface(null, Typeface.BOLD)
            val llp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            llp.setMargins(16, 16, 16, 16)
            result.layoutParams = llp
            return result
        }

        fun generateCopyright(text: String, context: Context) : TextView {
            val result = TextView(context)
            result.text = text
            result.textSize = 16.0F
            result.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            val llp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            llp.setMargins(16, 30, 16, 16)
            result.layoutParams = llp
            result.setOnClickListener {
                Injection.provideToaster().show(context, "v1.0 DEV")
            }
            return result
        }

    }
}