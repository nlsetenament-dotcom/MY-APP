package com.nls.chat.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun Context.toast(msg: String, long: Boolean = false) {
    Toast.makeText(this, msg, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(msg: String) = requireContext().toast(msg)

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
