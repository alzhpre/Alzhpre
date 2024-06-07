package com.example.alzhpre

import android.app.Notification.Action
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController

import com.example.alzhpre.databinding.ActivityPantallaPrincipalFamiliarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class PantallaPrincipalFamiliarActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPantallaPrincipalFamiliarBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val handler = Handler(Looper.getMainLooper())


    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityPantallaPrincipalFamiliarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference


        val drawerLayout: DrawerLayout = binding.drawerLayoutFamiliar
        val navView: NavigationView = binding.navViewFamiliar
        val navController = findNavController(R.id.nav_host_fragment_content_pantalla_principal_familiar)

        val headerView = navView.getHeaderView(0)
        val username = headerView.findViewById<TextView>(R.id.user_name)
        val email = headerView.findViewById<TextView>(R.id.email)

        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid

        cargarImagenPerfil()

        handler.postDelayed(object : Runnable {
            override fun run() {
                cargarImagenPerfil()
                handler.postDelayed(this, 5000)
            }
        }, 5000)


        val userReference = database.child("FamilyUsers").child(currentUserUid).child("Profile")
        userReference.child("username").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val usernameValue = dataSnapshot.getValue(String::class.java)
                username.text = usernameValue

                val userRef = database.child("Resultados").child(usernameValue.toString()).child("Clics")

                userRef.child("ClicsIncorrectos").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val resultClicsIncorrectos = dataSnapshot.getValue(Int::class.java) ?: 0 // Manejar null como 0
                        val userRef1 = database.child("Resultados").child(usernameValue.toString()).child("Clics")

                        userRef1.child("ClicsCorrectos").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val resultClicsCorrectos = dataSnapshot.getValue(Int::class.java) ?: 0 // Manejar null como 0

                                // Evitar división por cero
                                val rootView: View = findViewById(android.R.id.content)
                                if (resultClicsCorrectos != 0) {
                                    val resultadoError = (resultClicsIncorrectos.toDouble() / (resultClicsCorrectos.toDouble()+ resultClicsIncorrectos.toDouble())) * 100
                                    val aux = String.format("%.2f", resultadoError)
                                    mostrarSnackbar(rootView, "El usuari te un percentatje d'error de clics de $aux %", R.color.black, R.color.white)

                                    binding.appBarPantallaPrincipalFamiliar.fab.setOnClickListener { view ->
                                        mostrarSnackbar(rootView, "El usuari te un percentatje d'error de clics de $aux %", R.color.black, R.color.white)
                                    }
                                } else {
                                    mostrarSnackbar(rootView, "N/A", R.color.black, R.color.white)
                                }

                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Manejar el error
                                Toast.makeText(this@PantallaPrincipalFamiliarActivity, "Error al cargar los datos: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejar el error
                        Toast.makeText(this@PantallaPrincipalFamiliarActivity, "Error al cargar los datos: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Maneja el error
            }
        })

        userReference.child("email").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val emailValue = dataSnapshot.getValue(String::class.java)
                email.text = emailValue

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Maneja el error
            }
        })




        cargarImagenPerfil()
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_graph_familiar, R.id.nav_about_familiar, R.id.nav_logout_familiar
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.pantalla_principal_familiar, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_pantalla_principal_familiar)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun cargarImagenPerfil() {
        val currentUserUid = firebaseAuth.currentUser?.uid ?: return
        val navView: NavigationView = binding.navViewFamiliar
        val headerView = navView.getHeaderView(0)
        val imagenPerfil = headerView.findViewById<CircleImageView>(R.id.imagenPerfil)

        val userReference = database.child("FamilyUsers").child(currentUserUid).child("Imagen").child("imag")
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val imagen = dataSnapshot.getValue(String::class.java)
                val resId = when (imagen) {
                    "avatar1" -> R.drawable.avatar1
                    "avatar2" -> R.drawable.avatar2
                    "avatar3" -> R.drawable.avatar3
                    "avatar4" -> R.drawable.avatar4
                    else -> R.drawable.profile // Imagen predeterminada
                }
                Glide.with(this@PantallaPrincipalFamiliarActivity)
                    .load(resId)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(imagenPerfil)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PantallaPrincipalActivity", "Error al cargar la imagen de perfil: ${databaseError.message}")
            }
        })
    }

    fun mostrarSnackbar(view: View, mensaje: String, colorFondoResId: Int, colorTextoResId: Int) {
        val snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        // Cambiar el color de fondo del Snackbar
        snackbarView.setBackgroundColor(ContextCompat.getColor(view.context, colorFondoResId))

        // Cambiar el color del texto y centrar el texto
        val snackbarTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackbarTextView.setTextColor(ContextCompat.getColor(view.context, colorTextoResId))
        snackbarTextView.textSize = 25F
        snackbarTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER

        // Centrar el Snackbar en la pantalla
        val params = snackbarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER
        snackbarView.layoutParams = params

        snackbar.show()
    }

}