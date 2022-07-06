package vn.com.galaxy.log.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.com.galaxy.log.data.data.local.TrackingLogLocalSource
import vn.com.galaxy.log.data.data.remote.TrackingLogRemoteSource
import vn.com.galaxy.log.data.repositories.TrackingLogApiRepository
import vn.com.galaxy.log.data.repositories.TrackingLogApiRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrackingLogRepositoryModule {

    @Singleton
    @Provides
    fun provideTrackingLogRepository(
        localSource: TrackingLogLocalSource,
        remoteSource: TrackingLogRemoteSource
    ): TrackingLogApiRepository =
        TrackingLogApiRepositoryImpl(localSource, remoteSource)
}