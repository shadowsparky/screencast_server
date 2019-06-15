/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.custom_views

import android.content.Context
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

/**
 * Элемент настроек
 *
 * @param context контекст
 * @param parent родительский лейаут
 * @param choosed_item выбранный элемент
 * @param handler обратный вызов. Срабатывает при смене настроек
 * @property mSettingName наименование настройки
 * @property mCurrentSetting текущее значение
 * @property mCard карточка
 * @since v1.0.0
 * @author shadowsparky
 */
class SettingsItem(context: Context, parent: LinearLayout, choosed_item: SettingsChoose, handler: Settingeable) : LinearLayout(context) {
    private val view: View
    val mSettingName: TextView
    val mCurrentSetting: TextView
    private val mCard: CardView

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.settings_item, parent, false) // вставка элемента с настройкой во view
        /* поиск элементов вставленного view */
        this.mSettingName = view.findViewById(R.id.setting_name)
        this.mCurrentSetting = view.findViewById(R.id.current_setting)
        this.mCard = view.findViewById(R.id.setting_card)
        this.mCard.setOnClickListener {
            handler.onSettingChoosed(choosed_item) // при нажатии на карточку срабатывает callback
        }
        parent.addView(view) // добавление сгенерированного элемента в родительский лейаут
    }

    companion object {
        /**
         * Используется для генерации нового раздела
         *
         * @param section_name название нового раздела
         * @param context контекст
         * @return возвращает [TextView], стилизованный под новый раздел
         */
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

        /**
         * Используется для генерации копирайта
         *
         * @param text текст копирайта
         * @param context контекст
         * @return возвращает [TextView], стилизованный под копирайт
         */
        fun generateCopyright(text: String, context: Context) : TextView {
            val result = TextView(context)
            result.text = text
            result.textSize = 16.0F
            result.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            val llp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            llp.setMargins(16, 30, 16, 16)
            result.layoutParams = llp
            result.setOnClickListener {
                Injection.provideToaster().show(context, "^-^")
            }
            return result
        }

    }
}