package vn.com.galaxy.log.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object Utils {
    fun getAppVersion(context: Context): Long {
        var versionCode = -1L
        try {
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName, PackageManager.GET_META_DATA
                ).longVersionCode
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName, PackageManager.GET_META_DATA
                ).versionCode.toLong()
            }
        } catch (e: Exception) {
        }
        return versionCode
    }

    fun getAppVersionName(context: Context): String {
        return context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_META_DATA
        ).versionName
    }
}