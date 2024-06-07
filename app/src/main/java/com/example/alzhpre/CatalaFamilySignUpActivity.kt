package com.example.alzhpre

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alzhpre.data.FamilyUser
import com.example.alzhpre.databinding.CatalaActivityFamilySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CatalaFamilySignUpActivity: AppCompatActivity() {
    private lateinit var binding: CatalaActivityFamilySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.catala_activity_family_sign_up)
        binding = CatalaActivityFamilySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val username = binding.editUsername.text.toString()
            val familyEmail = binding.editfamilyEmail.text.toString()
            val email = binding.editEmail.text.toString()
            val pass = binding.editPassword.text.toString()
            val confirmPass = binding.editCPassword.text.toString()

            if (familyEmail.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    if (isValidPassword(pass)) {
                        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                            if (it.isSuccessful) {
                                val currentUser = firebaseAuth.currentUser
                                currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Registro exitoso. Por favor, verifica tu correo electrónico.", Toast.LENGTH_SHORT).show()
                                        saveFamilyUserToDatabase(username, familyEmail, email, pass)
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

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, CatalaFamilySignInActivity::class.java)
            startActivity(intent)
        }

    }

    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Z]).{5,}$")
        return regex.containsMatchIn(password)
    }

    private fun saveFamilyUserToDatabase(username: String, familyUser: String, email: String, pass: String) {
        val currentUser = firebaseAuth.currentUser ?: return
        val currentUserUid = currentUser.uid
        database = FirebaseDatabase.getInstance().getReference("FamilyUsers")
        val user = FamilyUser(username, familyUser, email)
        database.child(currentUserUid).child("Profile").setValue(user).addOnSuccessListener {
            binding.editUsername.text?.clear()
            binding.editfamilyEmail.text?.clear()
            binding.editEmail.text?.clear()
            binding.editPassword.text?.clear()
            binding.editCPassword.text?.clear()

            Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Ha habido un error", Toast.LENGTH_SHORT).show()
        }
    }
}
