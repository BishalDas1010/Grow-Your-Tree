package com.example.disipline.data.Local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.disipline.data.Local.profile.BlockedAppEntity
import com.example.disipline.data.Local.profile.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    abstract fun observeProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM blocked_apps ORDER BY appName")
    abstract fun observeBlockedApps(): Flow<List<BlockedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    abstract suspend fun deleteBlockedApp(packageName: String)
}