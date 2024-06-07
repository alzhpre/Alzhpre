package com.example.alzhpre

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CatalaNivell2Activity: AppCompatActivity() {
    private lateinit var database: DatabaseReference
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catala_activity_nivell2)

        database = FirebaseDatabase.getInstance().reference
        val nivell2: View = findViewById(R.id.nivell2)
        val button1: Button = findViewById(R.id.btnPreguntesPersonals)
        val button2: Button = findViewById(R.id.btnMemoritzacio)
        val button3: Button = findViewById(R.id.btnCalculs)

        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid

        // Configurar OnClickListener para el primer botón
        findViewById<Button>(R.id.btnPreguntesPersonals).setOnClickListener {
            saveCorrectClick(currentUserUid)
            val intent = Intent(this, com.example.alzhpre.catala_nivell2.CatalaPreguntesPersonalsActivity::class.java)
            startActivity(intent)
        }

        // Configurar OnClickListener para el segundo botón
        findViewById<Button>(R.id.btnMemoritzacio).setOnClickListener {
            saveCorrectClick(currentUserUid)
            val intent = Intent(this, com.example.alzhpre.catala_nivell2.CatalaAsociarImagenesActivity::class.java)
            startActivity(intent)
        }

        // Configurar OnClickListener para el tercer botón
        findViewById<Button>(R.id.btnCalculs).setOnClickListener {
            saveCorrectClick(currentUserUid)
            val intent = Intent(this, com.example.alzhpre.catala_nivell2.CatalaCalculActivity::class.java)
            startActivity(intent)
        }
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