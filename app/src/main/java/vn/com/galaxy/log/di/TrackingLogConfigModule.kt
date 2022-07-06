package vn.com.galaxy.log.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.com.galaxy.log.BuildConfig
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object TrackingLogConfigModule {

    @Provides
    @Named(TrackingLogInjectionConstants.KEY_API_TRACKING)
    fun providesApiTracking(): String {
        return BuildConfig.API_TRACKING
    }

    @Provides
    @Named(TrackingLogInjectionConstants.KEY_DATABASE_NAME)
    fun providesDatabaseName(): String {
        return BuildConfig.DATABASE_NAME
    }

    @Provides
    @Named(TrackingLogInjectionConstants.KEY_PREFERENCES_NAME)
    fun providesSharedPrefName(): String {
        return BuildConfig.SHARED_PREFS_NAME
    }

    @Provides
    @Named(TrackingLogInjectionConstants.KEY_IS_DEBUG_BUILD)
    fun providesIsDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }
}