package com.example.tracking.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.tracking.db.TrackingDatabase
import com.example.tracking.db.UserInfo
import com.example.tracking.utils.Constants.TRACKING_DATABASE_NAME
import com.example.tracking.utils.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    fun provideTrackingDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        TrackingDatabase::class.java,
        TRACKING_DATABASE_NAME
    ).fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideTrackDao(db: TrackingDatabase) = db.getTrackDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideUserInfo(@ApplicationContext context: Context) = UserInfo(
        provideSharedPreferences(context)
    )
}
