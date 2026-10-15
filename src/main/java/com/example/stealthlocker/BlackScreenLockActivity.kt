package com.example.stealthlocker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * شاشة سوداء جديدة (كود جديد) تُعرض عند فتح الإذن وتطلب كلمة المرور.
 * لا تُعدّل الأكواد القديمة؛ تستخدم نفس التخطيط activity_password.
 */
class BlackScreenLockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val unlockButton = findViewById<Button>(R.id.unlockButton)
        val secretPassword = "123"

        unlockButton.setOnClickListener {
            if (passwordInput.text.toString() == secretPassword) {
                finish()
            } else {
                Toast.makeText(this, "كلمة السر خاطئة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        // منع زر الرجوع
    }
}
