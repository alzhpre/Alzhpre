package com.example.alzhpre

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alzhpre.data.FamilyUser
import com.example.alzhpre.data.User
import com.example.alzhpre.databinding.CatalaActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CatalaSignUpActivity: AppCompatActivity() {
    private lateinit var binding: CatalaActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var nivell: String
    private lateinit var btnNivell1: Button
    private lateinit var btnNivell2: Button
    private lateinit var btnNivell3: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.catala_activity_sign_up)
        binding= CatalaActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        nivell = "Nivell 1"
        btnNivell1 = findViewById(R.id.btnNivell1)
        btnNivell2 = findViewById(R.id.btnNivell2)
        btnNivell3 = findViewById(R.id.btnNivell3)

        btnNivell1.setOnClickListener{
            nivell = "Nivell 1"
        }
        btnNivell2.setOnClickListener{
            nivell = "Nivell 2"
        }
        btnNivell3.setOnClickListener{
            nivell = "Nivell 3"
        }

        binding.btnRegister.setOnClickListener {
            val username = binding.editUsername.text.toString()
            val email = binding.editEmail.text.toString()
            val pass = binding.editPassword.text.toString()
            val confirmPass = binding.editCPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    if (isValidPassword(pass)) {
                        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                            if (it.isSuccessful) {
                                val currentUser = firebaseAuth.currentUser
                                currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Registro exitoso. Por favor, verifica tu correo electrónico.", Toast.LENGTH_SHORT).show()
                                        saveUserToDatabase(username, email, nivell)
                                        val intent = Intent(this, CatalaSignInActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(this, "Error al enviar el correo de verificación.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "La contraseña debe tener al menos 5 caracteres y al menos una letra mayúscula", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Has de rellenar todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Z]).{5,}$")
        return regex.containsMatchIn(password)
    }

    private fun saveUserToDatabase(username: String, email: String, nivell: String) {
        database = FirebaseDatabase.getInstance().getReference("Users")
        val user = User(username, email, nivell)
        val currentUserUid = firebaseAuth.currentUser?.uid ?: return
        database.child(currentUserUid).child("Profile").setValue(user).addOnSuccessListener {
            binding.editUsername.text?.clear()
            binding.editEmail.text?.clear()
            binding.editPassword.text?.clear()
            binding.editCPassword.text?.clear()
            Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Ha habido un error", Toast.LENGTH_SHORT).show()
        }
    }
}
