package com.example.disipline.UI.blockapps

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (packageName == applicationContext.packageName) return

            val enabled = BlockedAppsStore.isEnabled(applicationContext)
            val blockedPackages = BlockedAppsStore.load(applicationContext)

            if (enabled && packageName in blockedPackages) {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)
            }
        }
    }

    override fun onInterrupt() {}
}
