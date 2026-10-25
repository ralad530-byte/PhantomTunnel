package com.shadow.vpnclient.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getById(id: Long): ServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(server: ServerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(servers: List<ServerEntity>): List<Long>

    @Update
    suspend fun update(server: ServerEntity)

    @Delete
    suspend fun delete(server: ServerEntity)

    @Query("DELETE FROM servers WHERE subscriptionId = :subId")
    suspend fun deleteBySubscription(subId: Long)

    @Query("UPDATE servers SET latencyMs = :latencyMs WHERE id = :id")
    suspend fun updateLatency(id: Long, latencyMs: Int)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY id ASC")
    fun observeAll(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sub: SubscriptionEntity): Long

    @Delete
    suspend fun delete(sub: SubscriptionEntity)

    @Query("UPDATE subscriptions SET lastUpdated = :ts WHERE id = :id")
    suspend fun touch(id: Long, ts: Long)
}
