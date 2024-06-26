package com.example.alzhpre.catala_nivell1

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.alzhpre.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class CatalaPreguntesPersonalsActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUser: FirebaseUser

    private lateinit var preguntaTextView: TextView
    private lateinit var opcionesButtons: List<Button>

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaPlayer2: MediaPlayer
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val mainHandler = android.os.Handler(Looper.getMainLooper())
    private var respuesta: String = ""
    private var comprovar: Boolean = false
    private var correctAnswers = 0
    private var incorrectAnswers = 0
    private val errorThreshold = 90 // Porcentaje de errores permitido

    @SuppressLint("CutPasteId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catala_activity_nivell1_preguntes_personals)
        mediaPlayer = MediaPlayer.create(this, R.raw.sound)
        mediaPlayer2 = MediaPlayer.create(this, R.raw.sound_error)
        // Inicialización de la referencia a la base de datos de Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Obtener el usuario actualmente autenticado
        currentUser = FirebaseAuth.getInstance().currentUser!!

        database = FirebaseDatabase.getInstance().reference
        val preguntespersonals: View = findViewById(R.id.preguntespersonals)
        val button1: Button = findViewById(R.id.button_1)
        val button2: Button = findViewById(R.id.button_2)
        val button3: Button = findViewById(R.id.button_3)

        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid


        // Inicializar vistas
        preguntaTextView = findViewById(R.id.textview_pregunta)
        opcionesButtons = listOf(
            findViewById(R.id.button_1),
            findViewById(R.id.button_2),
            findViewById(R.id.button_3)
        )

        preguntespersonals.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val x = motionEvent.x
                val y = motionEvent.y

                // Detectar si el clic fue en un botón
                val isClickOnButton = isClickInsideView(button1, x, y) ||
                        isClickInsideView(button2, x, y) ||
                        isClickInsideView(button3, x, y)

                if (!isClickOnButton) {
                    // Manejar clic incorrecto
                    saveIncorrectClick(currentUserUid)
                }
                else{
                    saveCorrectClick(currentUserUid)
                }
            }
            true
        }

       cridarFuncio()

    }


    private fun obtenerPreguntaAleatoria(): String {
        val tiposDePreguntas = mutableListOf(
            "aficioPreferida", "birthday", "birthplace", "brothersName", "childrenName", "colorPreferit", "coupleName", "fullName", "menjarPreferit", "musicaPreferida", "parentsName",
            "peliculaPreferida", "pets", "petsName", "professio", "residencePlace"
        )
        tiposDePreguntas.shuffle()
        return tiposDePreguntas.first()
    }

    private fun mostrarPreguntaYOpciones1(pregunta: String, pregunta1: String, pregunta2: String) {
        val num = mutableListOf(1,2,0)
        num.shuffle()
        val preguntaRef = databaseReference.child("Users").child(currentUser.uid).child("Questions").child(pregunta)
        val preguntaRef1 = databaseReference.child("Users").child(currentUser.uid).child("Questions").child(pregunta1)
        val preguntaRef2 = databaseReference.child("Users").child(currentUser.uid).child("Questions").child(pregunta2)

        // Reset buttons and text
        preguntaTextView.text = ""
        opcionesButtons.forEach {
            it.text = ""
            it.setBackgroundResource(R.color.azul_cielo)
        }

        preguntaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                respuesta = snapshot.getValue(String::class.java) ?: ""
                preguntaTextView.text = obtenerTextoPregunta(pregunta)

                // Seleccionar aleatoriamente 2 respuestas falsas y agregar la correcta

                opcionesButtons[num[0]].text = respuesta
                val currentUser = FirebaseAuth.getInstance().currentUser!!
                val currentUserUid = currentUser.uid
                opcionesButtons[num[0]].setOnClickListener {
                    if (opcionesButtons[num[0]].text == respuesta) {

                        opcionesButtons[num[0]].setBackgroundResource(R.color.color_verde)
                        mediaPlayer.start()
                        saveCorrectClick(currentUserUid)
                        guardarResultado("correcto")
                        val rootView: View = findViewById(android.R.id.content)
                        mostrarSnackbar(rootView, "La resposta $respuesta es correcta!", R.color.color_verde, R.color.black)
                        mainHandler.postDelayed({
                            cridarFuncio()
                        }, 1000)


                        comprovar = true

                    }
                    else {
                        opcionesButtons[num[0]].setBackgroundResource(R.color.color_rojo)
                        mediaPlayer2.start()
                        saveCorrectClick(currentUserUid)
                        guardarResultado("incorrecto")

                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores de lectura de la base de datos
            }
        })
        preguntaRef1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val respuestaCorrecta = snapshot.getValue(String::class.java) ?: ""
                preguntaTextView.text = obtenerTextoPregunta(pregunta)
                val currentUser = FirebaseAuth.getInstance().currentUser!!
                val currentUserUid = currentUser.uid
                // Seleccionar aleatoriamente 2 respuestas falsas y agregar la correcta
                val opcionesSeleccionadas = respuestaCorrecta

                opcionesButtons[num[1]].text = opcionesSeleccionadas

                opcionesButtons[num[1]].setOnClickListener {
                    if (opcionesButtons[num[1]].text == respuesta) {
                        opcionesButtons[num[1]].setBackgroundResource(R.color.color_verde)
                        mediaPlayer.start()
                        saveCorrectClick(currentUserUid)
                        val rootView: View = findViewById(android.R.id.content)
                        mostrarSnackbar(rootView, "La resposta $respuesta es correcta!", R.color.color_verde, R.color.black)
                        guardarResultado("correcto")
                        mainHandler.postDelayed({
                            cridarFuncio()
                        }, 1000)

                    } else {
                        opcionesButtons[num[1]].setBackgroundResource(R.color.color_rojo)
                        mediaPlayer2.start()
                        saveCorrectClick(currentUserUid)
                        guardarResultado("incorrecto")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores de lectura de la base de datos
            }
        })
        preguntaRef2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val respuestaCorrecta = snapshot.getValue(String::class.java) ?: ""
                preguntaTextView.text = obtenerTextoPregunta(pregunta)
                val currentUser = FirebaseAuth.getInstance().currentUser!!
                val currentUserUid = currentUser.uid
                // Seleccionar aleatoriamente 2 respuestas falsas y agregar la correcta
                val opcionesSeleccionadas = respuestaCorrecta

                opcionesButtons[num[2]].text = opcionesSeleccionadas

                opcionesButtons[num[2]].setOnClickListener {
                    if (opcionesButtons[num[2]].text == respuesta) {
                        opcionesButtons[num[2]].setBackgroundResource(R.color.color_verde)
                        mediaPlayer.start()
                        saveCorrectClick(currentUserUid)
                        val rootView: View = findViewById(android.R.id.content)
                        mostrarSnackbar(rootView, "La resposta $respuesta es correcta!", R.color.color_verde, R.color.black)
                        guardarResultado("correcto")
                        mainHandler.postDelayed({
                            cridarFuncio()
                        }, 1000)

                    } else {
                        opcionesButtons[num[2]].setBackgroundResource(R.color.color_rojo)
                        mediaPlayer2.start()
                        saveCorrectClick(currentUserUid)
                        guardarResultado("incorrecto")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores de lectura de la base de datos
            }
        })
    }

    private fun cridarFuncio(){
        // Obtener preguntas aleatorias
        val preguntaAleatoria = obtenerPreguntaAleatoria()

        preguntaTextView.text= preguntaAleatoria
        val respuestaAleatoriaMala1 = obtenerPreguntaAleatoria()
        val respuestaAleatoriaMala2 = obtenerPreguntaAleatoria()

        // Mostrar pregunta y opciones en las vistas

        mostrarPreguntaYOpciones1(preguntaAleatoria, respuestaAleatoriaMala1, respuestaAleatoriaMala2)

    }

    private fun guardarResultado(resultado: String) {
        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid
        val resultadoMap = hashMapOf(
            "palabra" to respuesta,
            "resultado" to resultado,
            "timestamp" to System.currentTimeMillis()
        )
        if (currentUserUid != null) {
            databaseReference.child("Users").child(currentUserUid).child("ResultsPreguntes").push().setValue(resultadoMap)
            databaseReference.child("Users").child(currentUserUid).child("Profile").child("familyUser").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val familyUser =  snapshot.getValue(String::class.java)
                    familyUser?.let {
                        databaseReference.child("Resultados").child(it).child("Preguntes").push().setValue(resultadoMap)
                        if (resultado == "correcto") {
                            correctAnswers++
                        } else {
                            incorrectAnswers++
                        }

                        val totalAnswers = correctAnswers + incorrectAnswers
                        val errorPercentage = (incorrectAnswers * 100) / totalAnswers

                        if (errorPercentage > errorThreshold) {
                            val mensaje = "L'usuari ha superat l'umbral de errors permesos."
                            sendNotificationToUser(familyUser, mensaje)
                        }
                    }
                }

            }
        }
    }
    private fun sendNotificationToUser(otherUser: String, message: String) {
        val remoteMessage = RemoteMessage.Builder(otherUser)
            .setMessageId(java.lang.String.valueOf(System.currentTimeMillis()))
            .addData("message", message)
            .build()
        try {
            FirebaseMessaging.getInstance().send(remoteMessage)
            println("Notificació enviada a l'usuari: $otherUser")
        } catch (e: Exception) {
            println("Error al enviar notificació a l'usuari: $otherUser")
        }
    }
    private fun obtenerTextoPregunta(pregunta: String): String {
        return when (pregunta) {
            "username" -> "Usuari"
            "fullName" -> "Nom i cognoms"
            "birthday" -> "Data de naixement"
            "birthplace" -> "Lloc de naixement"
            "residencePlace" -> "Lloc de residencia"
            "parentsName" -> "Nom dels pares"
            "brothersName" -> "Nom de germans/es"
            "coupleName" -> "Nom de la teva parella"
            "childrenName" -> "Nom dels teus fills/es"
            "pets" -> "Tens mascotes?"
            "petsName" -> "Nom de les teves mascotes"
            "professio" -> "A que et dediques/aves"
            "menjarPreferit" -> "Quin és el teu menjar preferit?"
            "colorPreferit" -> "Quin és el teu color preferit?"
            "peliculaPreferida" -> "Quina és la teva pel·licula preferida?"
            "musicaPreferida" -> "Quina és la teva canço preferida?"
            "aficioPreferida" -> "Quina és la teva afició preferida?"
            else -> "Pregunta"
        }
    }
    fun mostrarSnackbar(view: View, mensaje: String, colorFondoResId: Int, colorTextoResId: Int) {
        val snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        // Cambiar el color de fondo del Snackbar
        snackbarView.setBackgroundColor(ContextCompat.getColor(view.context, colorFondoResId))

        // Cambiar el color del texto y centrar el texto
        val snackbarTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackbarTextView.setTextColor(ContextCompat.getColor(view.context, colorTextoResId))
        snackbarTextView.textSize = 30F
        snackbarTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER

        // Centrar el Snackbar en la pantalla
        val params = snackbarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER
        snackbarView.layoutParams = params

        snackbar.show()
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
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid
        database.child("Users").child(currentUserUid).child("Profile").child("familyUser").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val familyUser = snapshot.getValue(String::class.java)
                familyUser?.let {
                    val userReference = database . child ("Users").child(userUid).child("Clics")
                        .child("ClicsIncorrectos")
                    val familiarReference =
                        database.child("Resultados").child(it).child("Clics")
                            .child("ClicsIncorrectos")
                    // Incrementar el contador de clics incorrectos
                    userReference.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var clickCount = task.result?.getValue(Int::class.java) ?: 0
                            clickCount++
                            userReference.setValue(clickCount)
                            familiarReference.setValue(clickCount)
                        }
                    }

                }
            }
        }
        database.child("Users").child(currentUserUid).child("Profile").child("familyEmail")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val familyEmail = snapshot.getValue(String::class.java)
                    familyEmail?.let {
                        database.child("Users").child(currentUserUid).child("Clics")
                            .child("ClicsCorrectos").get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    val clicsCorrectos =
                                        snapshot.getValue(Int::class.java)
                                    clicsCorrectos?.let {
                                        database.child("Users").child(currentUserUid)
                                            .child("Clics")
                                            .child("ClicsIncorrectos").get()
                                            .addOnSuccessListener { snapshot ->
                                                if (snapshot.exists()) {
                                                    val clicsIncorrectos =
                                                        snapshot.getValue(Int::class.java)
                                                    clicsIncorrectos?.let {

                                                        if(clicsCorrectos.toInt() != 0 && clicsIncorrectos.toInt() != 0) {
                                                            val totalAnswers =
                                                                clicsCorrectos.toInt() + clicsIncorrectos.toInt()
                                                            val errorPercentage =
                                                                (clicsIncorrectos.toInt() * 100) / totalAnswers

                                                            if (errorPercentage > 70 ) {
                                                                val mensaje =
                                                                    "L'usuari ha superat l'umbral de errors permesos amb un $errorPercentage"
                                                                sendEmail(
                                                                    familyEmail,
                                                                    mensaje
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                }
                            }
                    }
                }
            }
    }
    private fun sendEmail(recipient: String, message: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Solo las apps de correo deben manejar esto
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, "Notificació de clics alts")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar correu electrònic utilitzant..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No hi ha cap aplicació de correu instal·lada.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCorrectClick(userUid: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid
        database.child("Users").child(currentUserUid).child("Profile").child("familyUser").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val familyUser = snapshot.getValue(String::class.java)
                    familyUser?.let {
                        val userReference = database.child("Users").child(userUid).child("Clics")
                            .child("ClicsCorrectos")
                        val familiarReference =
                            database.child("Resultados").child(it).child("Clics")
                                .child("ClicsCorrectos")
                        // Incrementar el contador de clics incorrectos
                        userReference.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                var clickCount = task.result?.getValue(Int::class.java) ?: 0
                                clickCount++
                                userReference.setValue(clickCount)
                                familiarReference.setValue(clickCount)
                            }
                        }
                    }
                }
            }
    }
}










