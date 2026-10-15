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

        // 1. الحماية الأولى: منع أخذ لقطات شاشة أو ظهور التطبيق في قائمة المهام الحديثة (Recent Apps)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // 2. الحماية الثانية: إظهار التطبيق فوق قفل النظام وإيقاظ الشاشة ليعمل كقفل أساسي حصين
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContentView(R.layout.activity_password)

        // 3. التطوير الأمني: إعداد مدير الجهاز لتنفيذ "الإغفاء/القفل المفاجئ" للشاشة فور تشغيل التطبيق
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, MyAdminReceiver::class.java)

        if (devicePolicyManager.isAdminActive(compName)) {
            devicePolicyManager.lockNow() // قفل الشاشة فوراً لفرض سيطرة القفل التام
        } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "هذا الإذن ضروري لتنفيذ قفل الشاشة الشامل ومنع أي تجاوز.")
            }
            startActivity(intent)
        }

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val unlockButton = findViewById<Button>(R.id.unlockButton)
        val secretPassword = "123" // الرمز السري الحقيقي لتجاوز القفل

        unlockButton.setOnClickListener {
            if (passwordInput.text.toString() == secretPassword) {
                // عند إدخال الرمز الصحيح، يُفتح القفل ويُسمح بالدخول للهاتف
                finish()
            } else {
                Toast.makeText(this, "الرمز السري خاطئ! الهاتف سيظل مقفلاً بالكامل.", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. تعطيل زر الرجوع كلياً بالطريقة الحديثة لمنع الهروب
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // متعمد تركها فارغة لتعطيل الرجوع تماماً
            }
        })
    }

    // 5. الحماية القصوى ضد الخروج: مراقبة مغادرة الواجهة وإجبار التطبيق على البقاء في المقدمة دائماً كقفل نظام
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
