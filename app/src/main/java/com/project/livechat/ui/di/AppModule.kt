package com.project.livechat.ui.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.livechat.data.db.ContactDatabase
import com.project.livechat.ui.components.connection.ConnectivityObserver
import com.project.livechat.ui.components.connection.NetworkConnectivityObserver
import com.project.livechat.ui.utils.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesFirebaseAuthInstance(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providesFirebaseStoreInstance(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun providesNetworkConnectivityObserver(
        @ApplicationContext appContext: Context
    ): ConnectivityObserver = NetworkConnectivityObserver(appContext)

    @Provides
    @Singleton
    fun providesContactDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, ContactDatabase::class.java, DATABASE_NAME).build()

    @Provides
    @Singleton
    fun providesContactDao(
        database: ContactDatabase
    ) = database.contactDao()
}