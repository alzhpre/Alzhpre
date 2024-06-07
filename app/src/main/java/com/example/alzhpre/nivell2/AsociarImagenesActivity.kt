package com.example.alzhpre.nivell2
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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.alzhpre.R
import com.example.alzhpre.databinding.ActivityAsociarImagenesBinding
import com.example.alzhpre.databinding.Nivell2ActivityAsociarImagenesBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class AsociarImagenesActivity : AppCompatActivity() {

    // Lista de palabras
    private val palabras = listOf("Arbol", "Avion", "Casa", "Columpio", "Horno", "Llaves", "Semaforo", "Television", "Tren")

    // Lista de IDs de recursos de imágenes asociadas
    private val imagenes = listOf(
        R.drawable.nivell1_arbol,
        R.drawable.nivell1_avion,
        R.drawable.nivell1_casa,
        R.drawable.nivell1_columpio,
        R.drawable.nivell1_horno,
        R.drawable.nivell1_llaves,
        R.drawable.nivell1_semaforo,
        R.drawable.nivell1_television,
        R.drawable.nivell1_tren
    )

    // Vistas
    private lateinit var imageView: ImageView
    private lateinit var palabraTextView: TextView
    private lateinit var resultadoTextView: TextView
    private lateinit var ningunaButton: Button
    private lateinit var binding: Nivell2ActivityAsociarImagenesBinding
    private lateinit var opcionesButtons: List<Button>
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
    val currentUser = FirebaseAuth.getInstance().currentUser!!
    val currentUserUid = currentUser.uid

    @SuppressLint("CutPasteId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.nivell2_activity_asociar_imagenes)
        binding= Nivell2ActivityAsociarImagenesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference
        val asociarimagenes_2: View = findViewById(R.id.asociarimagenes_2)
        val button1: Button = findViewById(R.id.opcion1Button)
        val button2: Button = findViewById(R.id.opcion2Button)
        val button3: Button = findViewById(R.id.opcion3Button)
        val button4: Button = findViewById(R.id.opcion4Button)
        val button5: Button = findViewById(R.id.opcion5Button)
        val button6: Button = findViewById(R.id.opcion6Button)
        val button7: Button = findViewById(R.id.opcion7Button)
        val button8: Button = findViewById(R.id.opcion8Button)
        val button9: Button = findViewById(R.id.opcion9Button)


        // Inicialización de las vistas
        imageView = findViewById(R.id.imageView)
        palabraTextView = findViewById(R.id.palabraTextView)
        mediaPlayer = MediaPlayer.create(this, R.raw.sound)
        mediaPlayer2 = MediaPlayer.create(this, R.raw.sound_error)
        database = FirebaseDatabase.getInstance().reference

        firebaseAuth = FirebaseAuth.getInstance()


        opcionesButtons = listOf(
            findViewById(R.id.opcion1Button),
            findViewById(R.id.opcion2Button),
            findViewById(R.id.opcion3Button),
            findViewById(R.id.opcion4Button),
            findViewById(R.id.opcion5Button),
            findViewById(R.id.opcion6Button),
            findViewById(R.id.opcion7Button),
            findViewById(R.id.opcion8Button),
            findViewById(R.id.opcion9Button),

            )

        asociarimagenes_2.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val x = motionEvent.x
                val y = motionEvent.y

                // Detectar si el clic fue en un botón
                val isClickOnButton = isClickInsideView(button1, x, y) ||
                        isClickInsideView(button2, x, y) ||
                        isClickInsideView(button3, x, y) ||
                        isClickInsideView(button4, x, y) ||
                        isClickInsideView(button5, x, y) ||
                        isClickInsideView(button6, x, y) ||
                        isClickInsideView(button7, x, y) ||
                        isClickInsideView(button8, x, y) ||
                        isClickInsideView(button9, x, y)

                if (!isClickOnButton) {
                    // Manejar clic incorrecto
                    saveIncorrectClick(currentUserUid)
                }
            }
            true
        }

        // Mostrar la primera imagen y palabra
        mostrarSiguientePalabra()

    }

    private fun mostrarSiguientePalabra() {
        // Obtener un índice aleatorio
        val indice = (0 until palabras.size).random()

        // Mostrar la palabra y la imagen correspondiente
        respuestaCorrecta = palabras[indice]
        imageView.setImageResource(imagenes[indice])

        // Barajar las opciones de respuesta
        val opcionesDesordenadas = palabras.shuffled()
        verificarRespuesta()
        // Mostrar las opciones en los botones

    }

    // Método para verificar si la opción seleccionada es correcta
    private fun verificarRespuesta() {
        val opcionesDesordenadas = palabras.shuffled()
        opcionesButtons.forEachIndexed { index, button ->
            button.text = opcionesDesordenadas[index]
            button.setOnClickListener {
                if (isClickable) {
                    isClickable = false
                    if (opcionesDesordenadas[index] == respuestaCorrecta) {
                        // La respuesta es correcta
                        saveCorrectClick(currentUserUid)
                        val rootView: View = findViewById(android.R.id.content)
                        mostrarSnackbar(rootView, "La respuesta $respuestaCorrecta es correcta!", R.color.color_verde, R.color.black)
                        button.setBackgroundResource(R.color.color_verde)
                        palabraTextView.text = respuestaCorrecta
                        mediaPlayer.start()
                        guardarResultado("correcto")

                        mainHandler.postDelayed({
                            opcionesButtons.forEachIndexed { index, button ->

                                button.setBackgroundResource(R.color.azul_cielo)
                            }
                            palabraTextView.text = ""
                            mostrarSiguientePalabra()
                        }, 1000)
                    } else {
                        // La respuesta es incorrecta
                        val aux = opcionesDesordenadas[index]
                        val rootView: View = findViewById(android.R.id.content)
                        mostrarSnackbar(rootView, "La respuesta $aux es incorrecta!", R.color.color_rojo, R.color.black)
                        saveCorrectClick(currentUserUid)
                        respuestaCorrecta= aux
                        button.setBackgroundResource(R.color.color_rojo)
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
    private fun guardarResultado(resultado: String) {
        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid
        val resultadoMap = hashMapOf(
            "palabra" to respuestaCorrecta,
            "resultado" to resultado,
            "timestamp" to System.currentTimeMillis()
        )
        if (currentUserUid != null) {
            database.child("Users").child(currentUserUid).child("Results").push().setValue(resultadoMap)
            database.child("Users").child(currentUserUid).child("Profile").child("familyUser").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val familyUser =  snapshot.getValue(String::class.java)
                    familyUser?.let {
                        database.child("Resultados").child(it).child("Images").push().setValue(resultadoMap)
                        if (resultado == "correcto") {
                            correctAnswers++
                        } else {
                            incorrectAnswers++
                        }

                        val totalAnswers = correctAnswers + incorrectAnswers
                        val errorPercentage = (incorrectAnswers * 100) / totalAnswers

                        if (errorPercentage > errorThreshold) {
                            val mensaje = "El usuario ha superado el umbral de errores permitido."
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
            println("Notificación enviada al usuario: $otherUser")
        } catch (e: Exception) {
            println("Error al enviar notificación al usuario: $otherUser")
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
                                                                    "El usuario ha superado el umbral de errores permitido con un $errorPercentage"
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
            putExtra(Intent.EXTRA_SUBJECT, "Notificación de clics altos")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar correo electrónico usando..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No hay ninguna aplicación de correo instalada.", Toast.LENGTH_SHORT).show()
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