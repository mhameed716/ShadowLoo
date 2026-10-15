package com.example.stealthlocker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
    private val enteredPassword = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 1. إخفاء أزرار النظام وشريط الإشعارات
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

            // ربط الواجهة (تأكد أن ملف activity_password.xml يحتوي على الأزرار)
            setContentView(R.layout.activity_password)

            // 4. حماية الشاشة بالكامل ضد اللمس العشوائي في الخلفية
            val rootLayout = findViewById<View>(android.R.id.content)
            rootLayout?.setOnTouchListener { _, _ -> true }

            // 5. إدارة مسؤول الجهاز وطلب الصلاحية إذا لم تكن مفعلة لتظهر نافذة الأذونات
            devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            compName = ComponentName(this, MyAdminReceiver::class.java)

            if (devicePolicyManager.isAdminActive(compName)) {
                try {
                    devicePolicyManager.lockNow()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "هذا الإذن مطلوب لتفعيل قفل الشاشة الشامل وحماية التطبيق.")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val passwordInput = findViewById<EditText>(R.id.passwordInput)
            val unlockButton = findViewById<Button>(R.id.unlockButton)
            val btnClear = findViewById<Button>(R.id.btnClear)
            val secretPassword = "123" // الرمز السري

            // مصفوفة أزرار الأرقام الداخلية (تأكد من وجودها في xml)
            val numberButtons = listOf(
                R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
                R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
                R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8", R.id.btn9 to "9"
            )

            for ((id, digit) in numberButtons) {
                findViewById<Button>(id)?.setOnClickListener {
                    if (enteredPassword.length < 6) {
                        enteredPassword.append(digit)
                        updatePasswordDisplay(passwordInput)
                    }
                }
            }

            btnClear?.setOnClickListener {
                if (enteredPassword.isNotEmpty()) {
                    enteredPassword.deleteCharAt(enteredPassword.length - 1)
                    updatePasswordDisplay(passwordInput)
                }
            }

            unlockButton?.setOnClickListener {
                if (enteredPassword.toString() == secretPassword) {
                    finish() // فتح القفل عند صحة الرمز
                } else {
                    Toast.makeText(this, "كلمة السر خاطئة!", Toast.LENGTH_SHORT).show()
                    enteredPassword.clear()
                    updatePasswordDisplay(passwordInput)
                }
            }

            // 6. منع زر الرجوع نهائياً
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // معطل تماماً لمنع الهروب
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePasswordDisplay(editText: EditText?) {
        val masked = "*".repeat(enteredPassword.length)
        editText?.setText(masked)
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowIDs())
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            )
        }
    }

    private fun WindowInsetsController.hide(any: Any) {
        this.hide(WindowInsets.Type.systemBars() or WindowInsets.Type.navigationBars())
        this.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun WindowIDs(): Int {
        return 0
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

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
