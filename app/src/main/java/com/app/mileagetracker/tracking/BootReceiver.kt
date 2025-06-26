package com.app.mileagetracker.tracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.app.mileagetracker.utils.PermissionUtil

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed received")

            // ✅ Only start service if permissions are already granted
            if (PermissionUtil.hasAllPermissions(context)) {
                val serviceIntent = Intent(context, LocationService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } else {
                Log.w("BootReceiver", "Permissions not granted. Not starting service.")
            }
        }
    }
}
