package com.example.stealthlocker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasswordLockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val unlockButton = findViewById<Button>(R.id.unlockButton)
        val secretPassword = "123" // كلمة السر الوهمية

        unlockButton.setOnClickListener {
            if (passwordInput.text.toString() == secretPassword) {
                finish() // فتح القفل
            } else {
                Toast.makeText(this, "كلمة السر خاطئة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        // منع زر الرجوع
    }
}

