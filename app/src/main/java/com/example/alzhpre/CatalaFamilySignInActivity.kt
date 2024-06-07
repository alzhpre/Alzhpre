package com.example.alzhpre

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.alzhpre.databinding.CatalaActivityFamilySignInBinding
import com.google.firebase.auth.FirebaseAuth

class CatalaFamilySignInActivity : AppCompatActivity(){
    private lateinit var binding: CatalaActivityFamilySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.catala_activity_family_sign_in)
        binding= CatalaActivityFamilySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, CatalaSignUpActivity::class.java)
            startActivity(intent)

        }
        binding.btnLogin.setOnClickListener{
            val email = binding.edEmail.text.toString()
            val pass = binding.edPassword.text.toString()
            if(email.isNotEmpty()&& pass.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener{
                    if(it.isSuccessful){
                        val intent = Intent(this, CatalaPantallaPrincipalFamiliarActivity::class.java)
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
            val intent = Intent(this, CatalaFamilySignUpActivity::class.java)
            startActivity(intent)
        }
        binding.tvPacient.setOnClickListener{
            val intent2 = Intent(this, CatalaSignInActivity::class.java)
            startActivity(intent2)
        }
    }
}