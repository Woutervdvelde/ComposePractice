package com.example.composepractice.ui.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Find the activity context. If one isn't present, an exception is thrown.
 */
fun Context.requireActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No activity was present but it is required.")
}
