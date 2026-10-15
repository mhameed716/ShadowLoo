package com.example.stealthlocker

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class PasswordLockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. منع ظهور التطبيق في شاشة التطبيقات الحديثة (Recent Apps) ومنع أخذ لقطة شاشة
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // 2. جعل شاشة القفل تظهر فوق شاشة قفل النظام حتى لو أعيد تشغيل الهاتف
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // تشغيل خدمة التراكب لحجب شريط الإشعارات
        startService(Intent(this, OverlayLockService::class.java))

        setContentView(R.layout.activity_password)

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val unlockButton = findViewById<Button>(R.id.unlockButton)
        val secretPassword = "123" // كلمة السر المطلوبة

        unlockButton.setOnClickListener {
            if (passwordInput.text.toString() == secretPassword) {
                finish() // فتح القفل وإغلاق الشاشة
            } else {
                Toast.makeText(this, "كلمة السر خاطئة", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. منع زر الرجوع نهائياً بالطريقة الحديثة
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // لا يوجد كود هنا ليبقى معطلاً
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, OverlayLockService::class.java))
    }

    // 4. منع الخروج عبر زر الرئيسية (Home) أو التطبيقات الحديثة عبر مراقبة مغادرة الواجهة
    override fun onPause() {
        super.onPause()
        // إعادة إحضار التطبيق للمقدمة فوراً إذا حاول المستخدم الخروج
        val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        activityManager.moveTaskToFront(taskId, 0)
    }
}
