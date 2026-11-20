package ai.antigem.assistant.core

import android.app.Application
import android.content.Context
import ai.antigem.assistant.di.AppModule

class App : Application() {

    lateinit var appModule: AppModule

    override fun onCreate() {
        super.onCreate()
        appModule = AppModule(this)
        // Initialize database (warm up)
        appModule.database
    }
}

fun appContext(context: Context): App = context.applicationContext as App
