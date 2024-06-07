package com.example.alzhpre.catala_nivell1
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.alzhpre.R
import com.example.alzhpre.databinding.CatalaActivityCalculBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class CatalaCalculActivity : AppCompatActivity() {
    private lateinit var binding: CatalaActivityCalculBinding
    private var respuestaCorrecta: String = ""
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaPlayer2: MediaPlayer
    private var isClickable = true
    private val mainHandler = android.os.Handler(Looper.getMainLooper())
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private var correctAnswers = 0
    private var incorrectAnswers = 0
    private val errorThreshold = 90 // Porcentaje de errores permitido
    private val preciosCine = listOf(5, 7, 9)
    val currentUser = FirebaseAuth.getInstance().currentUser!!
    val currentUserUid = currentUser.uid

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.catala_activity_calcul)
        binding = CatalaActivityCalculBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaPlayer = MediaPlayer.create(this, R.raw.sound)
        mediaPlayer2 = MediaPlayer.create(this, R.raw.sound_error)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val calculactivity_1: View = findViewById(R.id.calculactivity_1)
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        val button3: Button = findViewById(R.id.button3)


        // Precios disponibles para cada botón

        comprovarCodi(button1, 0)
        comprovarCodi(button2, 1)
        comprovarCodi(button3, 2)

        calculactivity_1.setOnTouchListener { view, motionEvent ->
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
                } else {
                    saveCorrectClick(currentUserUid)
                }
            }
            true
        }


    }

    private fun comprovarCodi(btn: Button, numero: Int) {
        btn.setOnClickListener {
            val precioAleatorio = preciosCine[numero]
            // Generar un número aleatorio del 2 al 5
            val factorAleatorio = Random.nextInt(1, 5)
            // Calcular el precio final
            val precioFinal = precioAleatorio * factorAleatorio

            val textView1: TextView = findViewById(R.id.textViewPrecio)
            val textView2: TextView = findViewById(R.id.textViewCantidad)

            val rootView: View = findViewById(android.R.id.content)
            mostrarSnackbar(
                rootView,
                "Cuanto es $precioAleatorio * $factorAleatorio ?",
                R.color.teal_200,
                R.color.black
            )
            // Mostrar resultados en la interfaz de usuario
            textView1.text = "$precioAleatorio"
            textView2.text = "$factorAleatorio"

            binding.btnCalculs.setOnClickListener {
                if (isClickable) {
                    isClickable = false
                    val dato = binding.editDatos.text.toString()
                    if (dato == precioFinal.toString()) {
                        mostrarSnackbar(
                            rootView,
                            "La respuesta $respuestaCorrecta es correcta!",
                            R.color.color_verde,
                            R.color.black
                        )
                        saveCorrectClick(currentUserUid)
                        binding.btnCalculs.setBackgroundResource(R.color.color_verde)
                        mediaPlayer.start()
                        respuestaCorrecta = dato
                        guardarResultado("correcto")
                        mainHandler.postDelayed({
                            binding.btnCalculs.setBackgroundResource(R.drawable.fondo_humo_pequeno)

                            limpiarResultados()
                        }, 1000)

                    } else {
                        val aux = precioFinal.toString()
                        val rootView: View = findViewById(android.R.id.content)
                        mostrarSnackbar(
                            rootView,
                            "La respuesta $aux es incorrecta!",
                            R.color.color_rojo,
                            R.color.black
                        )
                        binding.btnCalculs.setBackgroundResource(R.color.color_rojo)
                        saveCorrectClick(currentUserUid)
                        mediaPlayer2.start()
                        guardarResultado("incorrecto")
                    }
                    mainHandler.postDelayed({
                        isClickable =
                            true // Habilita nuevamente el clic del botón después del retraso
                    }, 1000)

                }
            }
        }
    }

    private fun limpiarResultados() {
        binding.textViewPrecio.text = ""
        binding.textViewCantidad.text = ""
        binding.editDatos.text?.clear()


    }

    private fun guardarResultado(resultado: String) {
        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid
        val resultadoMap = hashMapOf(
            "palabra" to respuestaCorrecta,
            "resultado" to resultado,
            "timestamp" to System.currentTimeMillis()
        )
        if (currentUserUid != null) {
            database.child("Users").child(currentUserUid).child("ResultsCalculs").push()
                .setValue(resultadoMap)
            database.child("Users").child(currentUserUid).child("Profile").child("familyUser").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val familyUser = snapshot.getValue(String::class.java)
                        familyUser?.let {
                            database.child("Resultados").child(it).child("Calcul").push()
                                .setValue(resultadoMap)
                            if (resultado == "correcto") {
                                correctAnswers++
                            } else {
                                incorrectAnswers++
                            }

                            val totalAnswers = correctAnswers + incorrectAnswers
                            val errorPercentage = (incorrectAnswers * 100) / totalAnswers

                            if (errorPercentage > errorThreshold) {
                                val mensaje =
                                    "El usuario ha superado el umbral de errores permitido."
                                sendNotificationToUser(familyUser, mensaje)
                            }
                        }
                    }

                }
        }
    }

    private fun sendNotificationToUser(otherUserUid: String, message: String) {
        val remoteMessage = RemoteMessage.Builder(otherUserUid)
            .setMessageId(java.lang.String.valueOf(System.currentTimeMillis()))
            .addData("message", message)
            .build()
        try {
            FirebaseMessaging.getInstance().send(remoteMessage)
            println("Notificación enviada al usuario: $otherUserUid")
        } catch (e: Exception) {
            println("Error al enviar notificación al usuario: $otherUserUid")
        }
    }

    fun mostrarSnackbar(view: View, mensaje: String, colorFondoResId: Int, colorTextoResId: Int) {
        val snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        // Cambiar el color de fondo del Snackbar
        snackbarView.setBackgroundColor(ContextCompat.getColor(view.context, colorFondoResId))

        // Cambiar el color del texto y centrar el texto
        val snackbarTextView =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
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
        database.child("Users").child(currentUserUid).child("Profile").child("familyUser").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val familyUser = snapshot.getValue(String::class.java)
                    familyUser?.let {
                        val userReference = database.child("Users").child(userUid).child("Clics")
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
                                                                    "El usuari ha superat el umbral de errors permès amb un $errorPercentage"
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
            startActivity(Intent.createChooser(emailIntent, "Enviar correu electrònic usant..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No hi ha cap aplicació de correu instal·lada.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCorrectClick(userUid: String) {
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
