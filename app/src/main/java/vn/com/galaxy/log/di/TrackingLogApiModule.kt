package vn.com.galaxy.log.di

import java.util.concurrent.TimeUnit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import vn.com.galaxy.log.data.data.remote.TrackingLogRemoteSource
import vn.com.galaxy.log.data.data.remote.TrackingLogRemoteSourceImpl
import vn.com.galaxy.log.data.data.remote.TrackingLogService
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrackingLogApiModule {

    @Provides
    @Singleton
    fun providesLogApiHttpLoggingInterceptor(
        @Named(TrackingLogInjectionConstants.KEY_IS_DEBUG_BUILD) debugBuild: Boolean
    ): Interceptor {
        return HttpLoggingInterceptor().apply {
            level = if (debugBuild) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideLogApiOkHttpClient(interceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideLogApiRetrofit(
        @Named(TrackingLogInjectionConstants.KEY_API_TRACKING) baseUrl: String,
        client: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideLogApiService(retrofit: Retrofit): TrackingLogService {
        return retrofit.create(TrackingLogService::class.java)
    }

    @Singleton
    @Provides
    fun provideTrackingLogRemoteSource(service: TrackingLogService): TrackingLogRemoteSource =
        TrackingLogRemoteSourceImpl(service)
}