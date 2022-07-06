package vn.com.galaxy.log.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.com.galaxy.log.data.data.local.TrackingLogDatabase
import vn.com.galaxy.log.data.data.local.TrackingLogLocalSource
import vn.com.galaxy.log.data.data.local.TrackingLogLocalSourceImpl
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrackingLogDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @Named(TrackingLogInjectionConstants.KEY_PREFERENCES_NAME) name: String,
        app: Application
    ): TrackingLogDatabase {
        return Room
            .databaseBuilder(
                app,
                TrackingLogDatabase::class.java,
                name
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTrackingLogLocalSource(database: TrackingLogDatabase): TrackingLogLocalSource =
        TrackingLogLocalSourceImpl(database)
}