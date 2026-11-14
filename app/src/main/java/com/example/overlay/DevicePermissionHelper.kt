package com.example.overlay

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

object DevicePermissionHelper {

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
            } catch (_: Exception) {
                true
            }
        } else {
            true
        }
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasEssentialPermissions(context: Context): Boolean {
        return canDrawOverlays(context) && isIgnoringBatteryOptimizations(context)
    }

    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                // Fallback to generic overlay settings if package URI fails
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    openAppDetails(context)
                }
            }
        }
    }

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallbackIntent)
                } catch (_: Exception) {
                    openAppDetails(context)
                }
            }
        }
    }

    fun openOemBackgroundSettings(context: Context) {
        // 1. Infinix / Tecno (Transsion XOS / HiOS PhoneMaster)
        val transsionIntents = listOf(
            Intent().setComponent(
                ComponentName(
                    "com.transsion.phonemaster",
                    "com.cyin.himgr.autostart.AutoStartActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.transsion.phonemaster",
                    "com.cyin.himgr.widget.activity.MainActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.transsion.phonemaster",
                    "com.cyin.himgr.MainActivity"
                )
            )
        )

        for (intent in transsionIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Try next Transsion component
            }
        }

        // Other OEM AutoStart managers if available (Xiaomi, Oppo, Vivo, Huawei, Samsung)
        val otherOemIntents = listOf(
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"))
        )
        for (intent in otherOemIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Try next OEM intent
            }
        }

        // 2. Battery optimization exemption directly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val batteryIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(batteryIntent)
                return
            } catch (_: Exception) {
                try {
                    val genericBatteryIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(genericBatteryIntent)
                    return
                } catch (_: Exception) {
                    // Fallback to app details
                }
            }
        }

        // 3. Application details settings
        try {
            val detailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(detailsIntent)
        } catch (_: Exception) {
            val generalIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(generalIntent)
        }
    }

    fun openAutostartSettings(context: Context) {
        openOemBackgroundSettings(context)
    }

    fun openXiaomiPopUpPermission(context: Context) {
        val intents = listOf(
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
            },
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
            }
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Try next
            }
        }

        openAppDetails(context)
    }

    fun openAppDetails(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Last resort: general settings
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun isXiaomi(): Boolean {
        val brand = Build.MANUFACTURER?.lowercase() ?: ""
        return brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco")
    }

    fun isSamsung(): Boolean {
        return Build.MANUFACTURER?.lowercase()?.contains("samsung") == true
    }

    fun isOppoOrVivo(): Boolean {
        val brand = Build.MANUFACTURER?.lowercase() ?: ""
        return brand.contains("oppo") || brand.contains("vivo") || brand.contains("realme") || brand.contains("oneplus")
    }

    fun isInfinixOrTecno(): Boolean {
        val brand = Build.MANUFACTURER?.lowercase() ?: ""
        val brandModel = (Build.BRAND + " " + Build.MODEL).lowercase()
        return brand.contains("infinix") || brand.contains("tecno") || brand.contains("transsion") || brand.contains("itel") ||
                brandModel.contains("infinix") || brandModel.contains("tecno") || brandModel.contains("transsion")
    }

    fun getDeviceBrandDisplayName(): String {
        val brand = Build.MANUFACTURER?.lowercase() ?: ""
        return when {
            isInfinixOrTecno() -> "Infinix / Tecno (XOS / HiOS)"
            isXiaomi() -> "Xiaomi / Redmi / POCO"
            isSamsung() -> "Samsung Galaxy"
            isOppoOrVivo() -> "Oppo / Vivo / Realme"
            brand.isNotBlank() -> brand.replaceFirstChar { it.uppercase() }
            else -> "جهاز أندرويد"
        }
    }
}
