package com.raywenderlich.currencyapp.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager {
        return getApplication(context).getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
    }

    @Singleton
    @Provides
    fun providesSharedPreferences (
        @ApplicationContext context: Context
    ) = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun providesEditor (sp: SharedPreferences) = sp.edit()

}