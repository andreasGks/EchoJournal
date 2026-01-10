package com.andreasgks.echojournal.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    fun startSplashTimer(onFinish: () -> Unit) {
        viewModelScope.launch {
            delay(2000)
            onFinish()
        }
    }
}
