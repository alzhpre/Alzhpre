package com.example.alzhpre

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController

import com.example.alzhpre.databinding.CatalaActivityPantallaPrincipalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class CatalaPantallaPrincipalActivity: AppCompatActivity() {
    private lateinit var binding: CatalaActivityPantallaPrincipalBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val handler = Handler(Looper.getMainLooper())


    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = CatalaActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        binding.appBarPantallaPrincipal.fab.setOnClickListener { view ->
            showTutorial()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.catala_nav_host_fragment_content_pantalla_principal)

        val headerView = navView.getHeaderView(0)
        val username = headerView.findViewById<TextView>(R.id.user_name)
        var porcentageError = headerView.findViewById<TextView>(R.id.porcentageError)


        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentUserUid = currentUser.uid

        cargarImagenPerfil()

        handler.postDelayed(object : Runnable {
            override fun run() {
                cargarImagenPerfil()
                handler.postDelayed(this, 5000)
            }
        }, 5000)




        val userReference = database.child("Users").child(currentUserUid).child("Profile")
        userReference.child("username").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val usernameValue = dataSnapshot.getValue(String::class.java)
                username.text = usernameValue
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Maneja el error
            }
        })

        val userRef = database.child("Users").child(currentUserUid).child("Clics")

        userRef.child("ClicsIncorrectos").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val resultClicsIncorrectos = dataSnapshot.getValue(Int::class.java) ?: 0 // Manejar null como 0
                val userRef1 = database.child("Users").child(currentUserUid).child("Clics")

                userRef1.child("ClicsCorrectos").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val resultClicsCorrectos = dataSnapshot.getValue(Int::class.java) ?: 0 // Manejar null como 0

                        // Evitar división por cero
                        if (resultClicsCorrectos != 0) {
                            val aux = (resultClicsIncorrectos.toDouble() / (resultClicsCorrectos.toDouble()+ resultClicsIncorrectos.toDouble())) * 100
                            porcentageError.text = String.format("%.2f%%", aux) // Mostrar con dos decimales
                        } else {
                            porcentageError.text = "N/A" // Mostrar N/A si no hay clics correctos
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejar el error
                        Toast.makeText(this@CatalaPantallaPrincipalActivity, "Error al cargar los datos: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar el error
                Toast.makeText(this@CatalaPantallaPrincipalActivity, "Error al cargar los datos: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })


        cargarImagenPerfil()
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,R.id.nav_home, R.id.nav_about, R.id.nav_graph, R.id.nav_logout
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.pantalla_principal, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.catala_nav_host_fragment_content_pantalla_principal)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun cargarImagenPerfil() {
        val currentUserUid = firebaseAuth.currentUser?.uid ?: return
        val navView: NavigationView = binding.navView
        val headerView = navView.getHeaderView(0)
        val imagenPerfil = headerView.findViewById<CircleImageView>(R.id.imagenPerfil)

        val userReference = database.child("Users").child(currentUserUid).child("Imagen").child("imag")
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
                Glide.with(this@CatalaPantallaPrincipalActivity)
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

    private fun showTutorial() {
        val tutorialFragment = TutorialStepFragment.newInstance(1)
        tutorialFragment.show(supportFragmentManager, "tutorial_step_1")
    }



}