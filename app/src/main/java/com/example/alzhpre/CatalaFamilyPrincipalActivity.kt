package com.example.alzhpre

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.alzhpre.databinding.ActivityFamilyPrincipalBinding
import com.example.alzhpre.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class CatalaFamilyPrincipalActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFamilyPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.catala_activity_family_principal)
        binding = ActivityFamilyPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button1.setOnClickListener {
            val intent = Intent(this, CatalaFamilySignInActivity::class.java)
            startActivity(intent)
        }

        binding.button2.setOnClickListener {
            val intent = Intent(this, CatalaFamilySignUpActivity::class.java)
            startActivity(intent)
        }
    }
}