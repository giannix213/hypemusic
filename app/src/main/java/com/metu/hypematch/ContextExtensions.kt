package com.metu.hypematch

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

/**
 * Extensiones de utilidad para Context
 */

/**
 * Encuentra la FragmentActivity anfitriona desde cualquier Context.
 * 
 * Esta función "desenvuelve" el Context hasta encontrar la Activity anfitriona.
 * Es útil cuando se trabaja con Views o AndroidView en Compose que pueden tener
 * el contexto envuelto en múltiples capas de ContextWrapper.
 * 
 * @return La FragmentActivity si se encuentra, null en caso contrario
 */
fun Context.findFragmentActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}
