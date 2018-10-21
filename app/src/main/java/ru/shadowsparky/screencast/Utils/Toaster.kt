package ru.shadowsparky.screencast.Utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

class Toaster {
    fun show(context: Context, message: String) =
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}