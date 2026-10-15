package com.example.stealthlocker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class PasswordLockActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. إخفاء أزرار التنقل الثلاثة وشريط الإشعارات نهائياً (مضاعفة تفعيل Immersive Mode)
        hideSystemUI()

        // 2. منع لقطات الشاشة أو ظهور التطبيق في المهام المفتوحة
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // 3. إظهار التطبيق فوق قفل النظام وإيقاظ الشاشة
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContentView(R.layout.activity_password)

        // 4. حماية مضاعفة 20 مرة للخلفية (إلغاء وتجاهل أي لمسة عشوائية خارج خانة النص وزر الفتح)
        val rootLayout = findViewById<View>(android.R.id.content)
        rootLayout.setOnTouchListener { v, event ->
            // تمرير اللمس فقط إذا كان على العناصر المسموح بها، وإلا فسيتم ابتلاع اللمسة وإلغاؤها تماماً
            true 
        }

        // 5. قفل الجهاز فجأة عند التشغيل لتفعيل الحماية
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, MyAdminReceiver::class.java)

        if (devicePolicyManager.isAdminActive(compName)) {
            devicePolicyManager.lockNow()
        }

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val unlockButton = findViewById<Button>(R.id.unlockButton)
        val secretPassword = "123" // كلمة السر السرية

        // السماح باللمس خصيصاً داخل خانة كتابة الرمز وزر الفتح
        passwordInput.setOnTouchListener { _, _ -> false }
        unlockButton.setOnTouchListener { _, _ -> false }

        unlockButton.setOnClickListener {
            if (passwordInput.text.toString() == secretPassword) {
                finish() // فتح القفل عند صحة الرمز
            } else {
                Toast.makeText(this, "كلمة السر خاطئة! الهاتف سيبقى مقفلاً.", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. منع زر الرجوع نهائياً
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // معطل تماماً
            }
        })
    }

    // دالة لإخفاء أزرار النظام وشريط الإشعارات نهائياً ومنع سحبها (مع تكرار الفحص لمضاعفة الأمان)
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
    }

    // ضمان بقاء أزرار النظام وشريط الإشعارات مخفية 20 مرة عند أي تفاعل أو تغيير في التركيز
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    // منع الخروج وإجبار التطبيق على البقاء دائماً في المقدمة
    override fun onPause() {
        super.onPause()
        try {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.moveTaskToFront(taskId, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
