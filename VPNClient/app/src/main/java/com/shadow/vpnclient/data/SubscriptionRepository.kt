package com.shadow.vpnclient.data

import android.util.Base64
import com.shadow.vpnclient.parser.UriParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Fetches a subscription URL (returns a base64 blob of newline-separated
 * share links, as used by v2rayN/v2rayNG-style subscriptions) and parses
 * every entry into ServerConfig via UriParser.
 */
class SubscriptionRepository(
    private val db: AppDatabase
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun refresh(subscriptionId: Long, url: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).header("User-Agent", "ShadowVPN/1.0").build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("empty body"))
            val decoded = try {
                String(Base64.decode(body.trim(), Base64.DEFAULT))
            } catch (e: Exception) {
                body // already plain text, one link per line
            }

            val links = decoded.lines().map { it.trim() }.filter { it.isNotBlank() }
            val servers = links.mapNotNull { UriParser.parse(it) }

            db.serverDao().deleteBySubscription(subscriptionId)
            val entities = servers.mapIndexed { idx, s ->
                ServerEntity.fromModel(s.copy(subscriptionId = subscriptionId), idx)
            }
            db.serverDao().insertAll(entities)
            db.subscriptionDao().touch(subscriptionId, System.currentTimeMillis())

            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
