package com.example.alzhpre.ui.graph

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.alzhpre.R
import com.example.alzhpre.databinding.FragmentGraphBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView


class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseResults: DatabaseReference
    private lateinit var databaseResultsPreguntes: DatabaseReference
    private lateinit var databaseResultsCalculs: DatabaseReference
    private lateinit var barChart1: BarChart
    private lateinit var barChart2: BarChart
    private lateinit var barChart3: BarChart
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(GraphViewModel::class.java)

        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        val root: View = binding.root
        firebaseAuth = FirebaseAuth.getInstance()
        barChart1 = binding.barChart1
        barChart2 = binding.barChart2
        barChart3 = binding.barChart3
        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid

        // Initialize Firebase Database for Results
        if (currentUserUid != null) {
            databaseResults = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserUid)
                .child("Results")
        }

        // Initialize Firebase Database for ResultsPreguntes
        if (currentUserUid != null) {
            databaseResultsPreguntes = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserUid)
                .child("ResultsPreguntes")
        }

        // Initialize Firebase Database for ResultsCalculs
        if (currentUserUid != null) {
            databaseResultsCalculs = FirebaseDatabase.getInstance().reference.child("Users").child(currentUserUid)
                .child("ResultsCalculs")
        }

        obtenerDatos()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun obtenerDatos() {
        databaseResults.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val resultados = snapshot.children.mapNotNull { it.toResultado() }
                mostrarGrafico(barChart1, resultados)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
            }
        })

        databaseResultsPreguntes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val resultadosPreguntes = snapshot.children.mapNotNull { it.toResultado() }
                mostrarGrafico(barChart2, resultadosPreguntes)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
            }
        })

        databaseResultsCalculs.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val resultadosCalculs = snapshot.children.mapNotNull { it.toResultado() }
                mostrarGrafico(barChart3, resultadosCalculs)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun DataSnapshot.toResultado(): Resultado? {
        try {
            val palabra = this.child("palabra").getValue(String::class.java)
            val resultado = this.child("resultado").getValue(String::class.java)
            val timestamp = this.child("timestamp").getValue(Long::class.java)
            Log.d("FirebaseData", "palabra: $palabra, resultado: $resultado, timestamp: $timestamp")
            return if (palabra != null && resultado != null && timestamp != null) {
                Resultado(palabra, resultado, timestamp)
            } else {
                Log.e("FirebaseData", "Null value detected")
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseData", "Error converting data", e)
            return null
        }
    }

    private fun mostrarGrafico(barChart: BarChart, resultados: List<Resultado>) {
        val correctos = resultados.count { it.resultado == "correcto" }
        val incorrectos = resultados.count { it.resultado == "incorrecto" }

        val entries = listOf(
            BarEntry(0f, correctos.toFloat()),
            BarEntry(1f, incorrectos.toFloat())
        )

        val dataSet = BarDataSet(entries, "Correctos")
        val data = BarData(dataSet)

        barChart.data = data
        barChart.invalidate()
    }


    data class Resultado(
        val palabra: String = "",
        val resultado: String = "",
        val timestamp: Long = 0L
    )
}
