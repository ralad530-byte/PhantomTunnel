package com.shadow.vpnclient.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ServerEntity::class, SubscriptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "shadow_vpn.db"
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
