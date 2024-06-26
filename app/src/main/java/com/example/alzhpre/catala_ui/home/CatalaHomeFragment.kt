package com.example.alzhpre.catala_ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.alzhpre.CatalaNivell1Activity
import com.example.alzhpre.CatalaNivell2Activity
import com.example.alzhpre.CatalaNivell3Activity
import com.example.alzhpre.R
import com.example.alzhpre.databinding.CatalaFragmentHomeBinding
import com.example.p1prova.catala_ui.home.CatalaHomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CatalaHomeFragment : Fragment() {

    private var _binding: CatalaFragmentHomeBinding? = null
    private lateinit var database: DatabaseReference

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(CatalaHomeViewModel::class.java)

        _binding = CatalaFragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Encuentra el botón por su ID
        val btnNormal: Button = view.findViewById(R.id.btnNormal)

        // Configura un OnClickListener para el botón
        btnNormal.setOnClickListener {
            // Crea un Intent para iniciar la actividad deseada
            val currentUser = FirebaseAuth.getInstance().currentUser!!
            val currentUserUid = currentUser.uid
            database = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid).child("Profile")
            database.child("nivell").get().addOnSuccessListener { dataSnapshot ->
                val nivellValue = dataSnapshot.value as? String
                if (nivellValue != null) {
                    when (nivellValue) {
                        "Nivell 1" -> {
                            val intent = Intent(requireContext(), CatalaNivell1Activity::class.java)
                            startActivity(intent)
                        }
                        "Nivell 2" -> {
                            val intent = Intent(requireContext(), CatalaNivell2Activity::class.java)
                            startActivity(intent)
                        }
                        "Nivell 3" -> {
                            val intent = Intent(requireContext(), CatalaNivell3Activity::class.java)
                            startActivity(intent)
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Nivel desconocido", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No se encontró nivel", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error al obtener el nivel", Toast.LENGTH_SHORT).show()
            }
        }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}