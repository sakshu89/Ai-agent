package ai.antigem.assistant.core

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val DATABASE_NAME = "antigem_assistant.db"
const val DATABASE_VERSION = 1

// Migration for future schema changes
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns/tables here when version bumps
    }
}

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        ConversationSummaryEntity::class,
        TaskEntity::class,
        TaskStepEntity::class,
        AutomationWorkflowEntity::class,
        SkillConfigEntity::class,
        AppConfigEntity::class,
        ServerConfigEntity::class,
        PreferenceEntity::class,
        AttachmentEntity::class,
        AgentExecutionStateEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun conversationSummaryDao(): ConversationSummaryDao
    abstract fun taskDao(): TaskDao
    abstract fun taskStepDao(): TaskStepDao
    abstract fun automationWorkflowDao(): AutomationWorkflowDao
    abstract fun skillConfigDao(): SkillConfigDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun serverConfigDao(): ServerConfigDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun agentExecutionStateDao(): AgentExecutionStateDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .enableMultiInstanceInvalidation()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
