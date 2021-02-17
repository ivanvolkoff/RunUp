package com.volkov.runup.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.volkov.runup.db.RunningDatabase
import com.volkov.runup.other.Constants.KEY_FIRTS_TIME_TOGGLE
import com.volkov.runup.other.Constants.KEY_NAME
import com.volkov.runup.other.Constants.KEY_WEIGHT
import com.volkov.runup.other.Constants.RUNNING_DATABASE_NAME
import com.volkov.runup.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDB(@ApplicationContext app: Context) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) = app.getSharedPreferences(
        SHARED_PREFERENCES_NAME, MODE_PRIVATE
    )

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT, 80f)
    @Singleton

    @Provides
    fun provideFirtsTimeTogle(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_FIRTS_TIME_TOGGLE, true)
}