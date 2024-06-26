package com.example.alzhpre
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alzhpre.databinding.CatalaActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class CatalaSignInActivity : AppCompatActivity(){
    private lateinit var binding: CatalaActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.catala_activity_sign_in)
        binding= CatalaActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, CatalaSignUpActivity::class.java)
            startActivity(intent)

        }
        binding.btnLogin.setOnClickListener {
            val email = binding.edEmail.text.toString()
            val pass = binding.edPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, CatalaQuestionsActivity::class.java)
                        startActivity(intent)
                    } else {
                        try {
                            throw it.exception!!
                        } catch (e: FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, "El correu electrònic no existeix", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Has de omplir tots els camps", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvPresentacio.setOnClickListener {
            binding.edEmail.setText("presentacio@gmail.com")
            binding.edPassword.setText("1234567")
        }

        binding.tvRegister.setOnClickListener{
            val intent = Intent(this, CatalaSignUpActivity::class.java)
            startActivity(intent)
        }
        binding.tvFamily.setOnClickListener{
            val intent2 = Intent(this, CatalaFamilyPrincipalActivity::class.java)
            startActivity(intent2)
        }
        binding.imageView1.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.imageView2.setOnClickListener {
            val intent = Intent(this, CatalaSignInActivity::class.java)
            startActivity(intent)
        }
    }
}