package vn.com.galaxy.trackinglogmanager.trackingmanager

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import vn.com.galaxy.trackinglogmanager.api.TrackingLogApiRepository
import vn.com.galaxy.trackinglogmanager.api.TrackingLogApiRepositoryImp
import vn.com.galaxy.trackinglogmanager.api.TrackingLogService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerBuilder {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideTrackingLogDao(appDatabase: AppDatabase): TrackingLogDao {
        return appDatabase.trackingLogDao()
    }

//    @Singleton
//    @Provides
//    fun provideRetrofit(): Retrofit {
//        val client: OkHttpClient = OkHttpClient.Builder()
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .build()
//        return Retrofit.Builder()
//            .client(client)
//            .baseUrl(BuildConfig.API_TRACKING)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }

    @Singleton
    @Provides
    fun prepareTrackingLogService(retrofit: Retrofit) : TrackingLogService {
        return retrofit.create(TrackingLogService::class.java)
    }

    @Singleton
    @Provides
    fun provideTrackingLogRepository(service : TrackingLogService) : TrackingLogApiRepository = TrackingLogApiRepositoryImp(service)
}