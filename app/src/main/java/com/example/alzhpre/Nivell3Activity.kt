package com.example.alzhpre

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Nivell3Activity: AppCompatActivity() {
    private lateinit var database: DatabaseReference
    @SuppressLint("ClickableViewAccessibility")
    private val mainHandler = android.os.Handler(Looper.getMainLooper())
    private val interactionTimeout = 15000L // 15 segundos
    private var lastInteractionTime = System.currentTimeMillis()
    private val interactionRunnable = Runnable {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInteractionTime >= interactionTimeout) {
            executeRandomActivity()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nivell3)

        database = FirebaseDatabase.getInstance().reference
        val nivell2: View = findViewById(R.id.nivell3)
        val button1: Button = findViewById(R.id.btnPreguntesPersonals)
        val button2: Button = findViewById(R.id.btnMemoritzacio)
        val button3: Button = findViewById(R.id.btnCalculs)

        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid

        // Llamar función del botón de preguntas personales
        preguntesPerso()

        // Llamar función del botón de memorización
        btnMemoritzacio()

        // Llamar función del botón de cálculos
        btnCalculs()

        // Si pasan 15 segundos y no ha interactuado, le llevará a una pestaña aleatoria.
        val rootView: View = findViewById(android.R.id.content)
        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                resetInteractionTimer()
            }
            false
        }

        // Iniciar el temporizador de inactividad
        startInteractionTimer()

        nivell2.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val x = motionEvent.x
                val y = motionEvent.y

                // Detectar si el clic fue en un botón
                val isClickOnButton = isClickInsideView(button1, x, y) || isClickInsideView(button2, x, y) || isClickInsideView(button3, x, y)

                if (!isClickOnButton) {
                    // Manejar clic incorrecto
                    saveIncorrectClick(currentUserUid)
                }

            }
            true
        }
    }

    override fun onPause() {
        super.onPause()
        stopInteractionTimer()
    }

    override fun onResume() {
        super.onResume()
        resetInteractionTimer()
    }

    private fun preguntesPerso() {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid
        findViewById<Button>(R.id.btnPreguntesPersonals).setOnClickListener {
            saveCorrectClick(currentUserUid)
            stopInteractionTimer()
            val intent = Intent(this, com.example.alzhpre.nivell3.PreguntesPersonalsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun btnMemoritzacio() {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid
        findViewById<Button>(R.id.btnMemoritzacio).setOnClickListener {
            saveCorrectClick(currentUserUid)
            stopInteractionTimer()
            val intent = Intent(this, com.example.alzhpre.nivell3.AsociarImagenesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun btnCalculs() {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid
        findViewById<Button>(R.id.btnCalculs).setOnClickListener {
            saveCorrectClick(currentUserUid)
            stopInteractionTimer()
            val intent = Intent(this, com.example.alzhpre.nivell3.CalculActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startInteractionTimer() {
        stopInteractionTimer()  // Detener el temporizador anterior si existe
        interactionRunnable.let { mainHandler.postDelayed(it, interactionTimeout) }
    }

    private fun resetInteractionTimer() {
        lastInteractionTime = System.currentTimeMillis()
        startInteractionTimer()
    }

    private fun stopInteractionTimer() {
        interactionRunnable.let { mainHandler.removeCallbacks(it) }
    }

    private fun executeRandomActivity() {
        stopInteractionTimer()
        val options = listOf(
            com.example.alzhpre.nivell3.PreguntesPersonalsActivity::class.java,
            com.example.alzhpre.nivell3.AsociarImagenesActivity::class.java,
            com.example.alzhpre.nivell3.CalculActivity::class.java
        )
        val randomActivity = options.random()
        val intent = Intent(this, randomActivity)
        startActivity(intent)
    }

    private fun isClickInsideView(view: View, x: Float, y: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]
        val viewWidth = view.width
        val viewHeight = view.height

        return x >= viewX && x <= viewX + viewWidth && y >= viewY && y <= viewY + viewHeight
    }

    private fun saveIncorrectClick(userUid: String) {
        val userReference = database.child("Users").child(userUid).child("Clics").child("ClicsIncorrectos")

        // Incrementar el contador de clics incorrectos
        userReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var clickCount = task.result?.getValue(Int::class.java) ?: 0
                clickCount++
                userReference.setValue(clickCount)
            }
        }
    }
    private fun saveCorrectClick(userUid: String) {
        val userReference = database.child("Users").child(userUid).child("Clics").child("ClicsCorrectos")

        // Incrementar el contador de clics incorrectos
        userReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var clickCount = task.result?.getValue(Int::class.java) ?: 0
                clickCount++
                userReference.setValue(clickCount)
            }
        }
    }
}