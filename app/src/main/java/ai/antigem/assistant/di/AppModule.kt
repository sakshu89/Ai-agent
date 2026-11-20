package ai.antigem.assistant.di

import android.content.Context
import ai.antigem.assistant.core.AppDatabase
import ai.antigem.assistant.core.AppRepository

class AppModule(private val context: Context) {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context.applicationContext)
    }

    val repository: AppRepository by lazy {
        AppRepository(database)
    }
}
