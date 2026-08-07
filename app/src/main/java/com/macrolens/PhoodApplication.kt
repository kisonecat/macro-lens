package com.macrolens

import android.app.Application
import com.macrolens.di.AppContainer

class PhoodApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
