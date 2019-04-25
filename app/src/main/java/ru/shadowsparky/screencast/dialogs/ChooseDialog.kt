/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.choose_dialog.*
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.extras.SettingsParser
import ru.shadowsparky.screencast.interfaces.ChangeSettingsHandler

class ChooseDialog(
        context_: Context,
        private val values: List<String>,
        private val handler: ChangeSettingsHandler,
        private val choose: SettingsChoose
) : Dialog(context_) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_dialog)
        this.setCanceledOnTouchOutside(false)
        setting_list.choiceMode = ListView.CHOICE_MODE_SINGLE
        cancel_setting_button.setOnClickListener {
            hide()
        }
        choosed_section.text = SettingsParser.getSectionName(choose)
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, values)
        setting_list.adapter = adapter
        setting_list.setOnItemClickListener { parent, view, position, id ->
            handler.onSettingsChanged(choose, values[position])
            this.hide()
        }
    }
}