/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import android.content.Context
import android.widget.Toast

class Toaster {
    fun show(context: Context, message: String) =
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}