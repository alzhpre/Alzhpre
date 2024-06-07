package com.example.alzhpre.catala_ui_familiar.graph

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.alzhpre.catala_ui_familiar.graph.CatalaGraphViewModel
import com.example.alzhpre.databinding.CatalaFragmentGraphFamiliarBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth


class CatalaGraphFamiliarFragment : Fragment() {

    private var _binding: CatalaFragmentGraphFamiliarBinding? = null
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
        val graphViewModel = ViewModelProvider(this).get(CatalaGraphViewModel::class.java)

        _binding = CatalaFragmentGraphFamiliarBinding.inflate(inflater, container, false)
        val root: View = binding.root
        firebaseAuth = FirebaseAuth.getInstance()
        barChart1 = binding.barChart1
        barChart2 = binding.barChart2
        barChart3 = binding.barChart3
        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference

        database.child("FamilyUsers").child(currentUserUid!!).child("Profile").child("username").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(String::class.java)
                user?.let {
                    databaseResults = FirebaseDatabase.getInstance().reference.child("Resultados")
                        .child(user)
                        .child("Images")

                    databaseResultsPreguntes =
                        FirebaseDatabase.getInstance().reference.child("Resultados")
                            .child(user)
                            .child("Preguntes")

                    databaseResultsCalculs =
                        FirebaseDatabase.getInstance().reference.child("Resultados")
                            .child(user)
                            .child("Calcul")


                    obtenerDatos()
                }


            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun obtenerDatos() {
        databaseResults.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val resultados = snapshot.children.mapNotNull {
                    parseResultado(it)
                }
                mostrarGrafico(barChart1, resultados)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
            }
        })

        databaseResultsPreguntes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val resultadosPreguntes = snapshot.children.mapNotNull {
                    parseResultado(it)
                }
                mostrarGrafico(barChart2, resultadosPreguntes)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
            }
        })

        databaseResultsCalculs.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val resultadosCalculs = snapshot.children.mapNotNull {
                    parseResultado(it)
                }
                mostrarGrafico(barChart3, resultadosCalculs)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseResultado(snapshot: DataSnapshot): Resultado? {
        return try {
            val palabra = snapshot.child("palabra").getValue(Any::class.java)
            val resultado = snapshot.child("resultado").getValue(Any::class.java)
            val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L

            palabra?.toString()?.let {
                resultado?.toString()?.let { it1 ->
                    Resultado(
                        palabra = it,
                        resultado = it1,
                        timestamp = timestamp
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("GraphFamiliarFragment", "Error parsing data: ${e.message}")
            null
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


