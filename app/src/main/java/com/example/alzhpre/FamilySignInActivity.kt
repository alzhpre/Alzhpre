package com.example.alzhpre

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alzhpre.databinding.ActivityFamilySignInBinding
import com.example.alzhpre.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class FamilySignInActivity : AppCompatActivity(){
    private lateinit var binding: ActivityFamilySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_family_sign_in)
        binding= ActivityFamilySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)

        }
        binding.btnLogin.setOnClickListener{
            val email = binding.edEmail.text.toString()
            val pass = binding.edPassword.text.toString()
            if(email.isNotEmpty()&& pass.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener{
                    if(it.isSuccessful){
                        val intent = Intent(this, PantallaPrincipalFamiliarActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                Toast.makeText(this, "Has de rellenar todos los campos", Toast.LENGTH_SHORT).show()
            }

        }
        binding.tvRegister.setOnClickListener{
            val intent = Intent(this, FamilySignUpActivity::class.java)
            startActivity(intent)
        }
        binding.tvPacient.setOnClickListener{
            val intent2 = Intent(this, SignInActivity::class.java)
            startActivity(intent2)
        }
    }
}