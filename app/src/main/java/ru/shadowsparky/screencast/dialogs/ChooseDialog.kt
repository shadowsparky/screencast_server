/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.choose_dialog.*
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.extras.SettingsParser
import ru.shadowsparky.screencast.interfaces.ChangeSettingsHandler

/**
 * Диалог, выскакивающий при смене настроек
 *
 * В нем отсутствуют кастомные методы, потому что они не нужны.
 * в [onCreate] инициализируется сам диалог, а позже, его можно вызвать с помощью метода [show], который относится к родительскому
 * классу [Dialog] из Android SDK.
 * @param context_ просто [Context], используемый для инициализации [Dialog]
 * @param values список значений, по которым строится список
 * @param handler обратный вызов, срабатывающий при смене настроек
 * @param choose выбранный раздел настроек
 * @since v1.0.0
 * @author shadowsparky
 * */
class ChooseDialog(
        context_: Context,
        private val values: List<String>,
        private val handler: ChangeSettingsHandler,
        private val choose: SettingsChoose
) : Dialog(context_) {

    /**
     * Система вызывает этот метод, когда создает фрагмент.
     * В своей реализации разработчик должен инициализировать ключевые компоненты фрагмента,
     * которые требуется сохранить, когда фрагмент находится в состоянии паузы или возобновлен после остановки.
     *
     * @see [Dialog]
     * @since v1.0.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_dialog) // применение лейаута диалога
        setting_list.choiceMode = ListView.CHOICE_MODE_SINGLE // выбрать можно только 1 вариант
        cancel_setting_button.setOnClickListener {
            this.dismiss() // при нажатии на "Отмена" диалог закрывается
        }
        choosed_section.text = SettingsParser.getSectionName(choose) // получение названия секции
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, values) // генерация адаптера с заданными значениями
        setting_list.adapter = adapter // применение адаптера
        setting_list.setOnItemClickListener { parent, view, position, id ->
            handler.onSettingsChanged(choose, values[position]) // при смене настроек сработает callback
            this.dismiss() // закрытие диалога
        }
    }
}