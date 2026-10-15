package com.example.stealthlocker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

        // 1. منع أخذ لقطات شاشة أو ظهور التطبيق في قائمة المهام المفتوحة
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // 2. إظهار الشاشة فوق قفل النظام وإيقاظ الشاشة
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContentView(R.layout.activity_password)

        // تشغيل خدمة التراكب (TYPE_APPLICATION_OVERLAY) لحجب شريط الإشعارات
        startService(Intent(this, OverlayLockService::class.java))

        // إعداد مدير الجهاز لقفل الهاتف فجأة عند تشغيل التطبيق إذا كان مفتوحاً
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, MyAdminReceiver::class.java)

        // إذا كان مسؤول الجهاز مفَعّلاً، نقوم بإغفاء/قفل الهاتف فجأة فوراً عند فتح التطبيق لفرض الحماية
        if (devicePolicyManager.isAdminActive(compName)) {
            // قفل الشاشة فوراً
            devicePolicyManager.lockNow()
        } else {
            // إذا لم يتم تفعيل المسؤول، نطلب من المستخدم تفعيله ليتمكن التطبيق من قفل الشاشة
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "هذا الإذن مطلوب لكي يتمكن التطبيق من قفل الشاشة وحمايتها تماماً.")
            }
            startActivity(intent)
        }

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val unlockButton = findViewById<Button>(R.id.unlockButton)
        val secretPassword = "123" // كلمة السر السرية الخاصة بك

        unlockButton.setOnClickListener {
            if (passwordInput.text.toString() == secretPassword) {
                finish() // فتح القفل والخروج عند صحة كلمة المرور
            } else {
                Toast.makeText(this, "كلمة السر خاطئة، حاول مجدداً", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. منع زر الرجوع كلياً بالطريقة الحديثة
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // متعمد تركها فارغة لمنع الرجوع
            }
        })
    }

    // 4. منع الخروج عبر زر الرئيسية وإجبار التطبيق على البقاء بالواجهة الأمامية
    override fun onPause() {
        super.onPause()
        try {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.moveTaskToFront(taskId, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, OverlayLockService::class.java))
    }
}
