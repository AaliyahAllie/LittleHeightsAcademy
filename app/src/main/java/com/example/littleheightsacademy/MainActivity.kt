package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.littleheightsacademy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle Register button
        binding.btnRegister.setOnClickListener {
            // Navigate to RegisterActivity (create this separately)
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Handle Parent Login
        binding.btnParentLogin.setOnClickListener {
            // Navigate to ParentLoginActivity
            val intent = Intent(this, ParentLoginActivity::class.java)
            startActivity(intent)
        }

        // Handle Admin Login
        binding.btnAdminLogin.setOnClickListener {
            // Navigate to AdminLoginActivity
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
        }
    }
}
