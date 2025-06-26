package com.app.mileagetracker.utils



import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtil {
    fun hasAllPermissions(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fg = ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE)
        val fgLocation = if (Build.VERSION.SDK_INT >= 34) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        } else PackageManager.PERMISSION_GRANTED

        return (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) &&
                fg == PackageManager.PERMISSION_GRANTED &&
                fgLocation == PackageManager.PERMISSION_GRANTED
    }
}
