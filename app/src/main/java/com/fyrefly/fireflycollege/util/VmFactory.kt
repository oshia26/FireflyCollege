package com.fyrefly.fireflycollege.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Small factory so ViewModels can receive the app container without a DI library. */
inline fun <reified VM : ViewModel> viewModelFactory(
    crossinline create: () -> VM
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
}
