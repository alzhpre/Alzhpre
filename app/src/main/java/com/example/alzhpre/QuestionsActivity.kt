package com.example.alzhpre

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alzhpre.data.Questions
import com.example.alzhpre.databinding.ActivityQuestionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class QuestionsActivity: AppCompatActivity() {

    private lateinit var binding: ActivityQuestionsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference


        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid



        val userQuestionsRef = currentUserUid?.let { database.child("Users").child(it).child("Questions") }

        binding.progressBar.visibility = View.VISIBLE
        binding.questionsForm.visibility = View.GONE
            userQuestionsRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // El usuario ya ha respondido las preguntas
                        val intent = Intent(this@QuestionsActivity, PantallaPrincipalActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.questionsForm.visibility = View.VISIBLE

                        binding.btnSave.setOnClickListener{
                            val nameandsurname = binding.nameandsurname.text.toString()
                            val birthday = binding.birthday.text.toString()
                            val birthplace = binding.birthplace.text.toString()
                            val residencePlace = binding.residencePlace.text.toString()
                            val parentsName = binding.parentsName.text.toString()
                            val brothersName = binding.brothersName.text.toString()
                            val coupleName = binding.coupleName.text.toString()
                            val childrenName = binding.childrenName.text.toString()
                            val pets = binding.pets.text.toString()
                            val petsName = binding.petsName.text.toString()
                            val professio = binding.professio.text.toString()
                            val menjarPreferit = binding.menjarPreferit.text.toString()
                            val colorPreferit = binding.colorPreferit.text.toString()
                            val peliculaPreferida = binding.peliculaPreferida.text.toString()
                            val musicaPreferida = binding.musicaPreferida.text.toString()
                            val aficioPreferida = binding.aficioPreferida.text.toString()

                            if (TextUtils.isEmpty(nameandsurname) || TextUtils.isEmpty(birthday) || TextUtils.isEmpty(birthplace) ||
                                TextUtils.isEmpty(residencePlace) || TextUtils.isEmpty(parentsName) || TextUtils.isEmpty(brothersName) ||
                                TextUtils.isEmpty(professio) || TextUtils.isEmpty(menjarPreferit) || TextUtils.isEmpty(colorPreferit) ||
                                TextUtils.isEmpty(peliculaPreferida) || TextUtils.isEmpty(musicaPreferida) || TextUtils.isEmpty(aficioPreferida)) {
                                Toast.makeText(this@QuestionsActivity, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                            }
                            // El usuario no ha respondido las preguntas, guardarlas en la base de datos
                            val questions = Questions(nameandsurname, birthday, birthplace, residencePlace, parentsName, brothersName, coupleName,
                                childrenName, pets, petsName, professio, menjarPreferit, colorPreferit, peliculaPreferida, musicaPreferida, aficioPreferida)
                            userQuestionsRef.setValue(questions).addOnSuccessListener {
                                clearFields()
                                Toast.makeText(this@QuestionsActivity, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@QuestionsActivity, PantallaPrincipalActivity::class.java)
                                startActivity(intent)
                            }.addOnFailureListener {
                                Toast.makeText(this@QuestionsActivity, "Ha habido un error", Toast.LENGTH_SHORT).show()
                            }
                            }
                        }
                    }
                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    binding.questionsForm.visibility = View.VISIBLE
                }
            })
        }
    private fun clearFields() {
        binding.nameandsurname.text?.clear()
        binding.birthday.text?.clear()
        binding.birthplace.text?.clear()
        binding.residencePlace.text?.clear()
        binding.parentsName.text?.clear()
        binding.brothersName.text?.clear()
        binding.coupleName.text?.clear()
        binding.childrenName.text?.clear()
        binding.pets.text?.clear()
        binding.petsName.text?.clear()
        binding.professio.text?.clear()
        binding.menjarPreferit.text?.clear()
        binding.colorPreferit.text?.clear()
        binding.peliculaPreferida.text?.clear()
        binding.musicaPreferida.text?.clear()
        binding.aficioPreferida.text?.clear()

    }
}



