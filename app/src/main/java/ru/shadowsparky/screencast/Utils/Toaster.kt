package ru.shadowsparky.screencast.Utils

import android.content.Context
import android.widget.Toast

class Toaster {
    fun show(context: Context, message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}