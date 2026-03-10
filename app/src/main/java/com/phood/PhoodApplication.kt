package com.phood

import android.app.Application
import com.phood.di.AppContainer

class PhoodApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
