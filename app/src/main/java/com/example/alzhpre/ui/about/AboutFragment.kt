package com.example.alzhpre.ui.about

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.alzhpre.R
import com.example.alzhpre.data.User
import com.example.alzhpre.databinding.FragmentAboutBinding
import com.example.alzhpre.ui.graph.GraphFragment
import com.example.p1prova.ui.about.AboutViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView


class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var dialog: Dialog
    private val handler = Handler(Looper.getMainLooper())
    private val PICK_IMAGE_REQUEST = 1


    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(AboutViewModel::class.java)
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        uid?.let { getUserData(it) }

        binding.profileImage.setOnClickListener {
            showAvatarSelectionDialog()
        }

        binding.saveBtn.setOnClickListener {
            saveUserData()
        }
        updateAvatar()


        handler.postDelayed(object : Runnable {
            override fun run() {
                updateAvatar()
                handler.postDelayed(this, 5000)
            }
        }, 5000)


    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return

        val username = binding.edUsername.text.toString()
        val email = binding.edEmail.text.toString()
        val pass = binding.edPassword.text.toString()
        val nivell = binding.edNivell.text.toString()
        val familyUser = binding.edUserFamiliar.text.toString()
        val familyEmail = binding.edEmailFamiliar.text.toString()
        val user = User(username, email, nivell)

        databaseReference.child(uid).child("Profile").setValue(user)
        databaseReference.child(uid).child("Profile").child("familyUser").setValue(familyUser)
        databaseReference.child(uid).child("Profile").child("familyEmail").setValue(familyEmail)

        if (pass.isNotEmpty() && isValidPassword(pass)) {
            updatePassword(pass)
        }
    }

    private fun updateAvatar() {
        val currentUserUid = auth.currentUser?.uid ?: return
        databaseReference.child(currentUserUid).child("Imagen").child("imag")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (view != null && isAdded) {
                        val imagen: String? = snapshot.getValue(String::class.java)
                        val resId = when (imagen) {
                            "avatar1" -> R.drawable.avatar1
                            "avatar2" -> R.drawable.avatar2
                            "avatar3" -> R.drawable.avatar3
                            "avatar4" -> R.drawable.avatar4
                            else -> R.drawable.profile // Cambia a un avatar predeterminado si es necesario
                        }
                        binding.profileImage.setImageResource(resId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Z]).{5,}$")
        return regex.containsMatchIn(password)
    }


    private fun showAvatarSelectionDialog() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_avatar_selection)
        dialog.findViewById<ImageView>(R.id.avatar1).setOnClickListener {
            selectAvatar(R.drawable.avatar1)
        }
        dialog.findViewById<ImageView>(R.id.avatar2).setOnClickListener {
            selectAvatar(R.drawable.avatar2)
        }
        dialog.findViewById<ImageView>(R.id.avatar3).setOnClickListener {
            selectAvatar(R.drawable.avatar3)
        }
        dialog.findViewById<ImageView>(R.id.avatar4).setOnClickListener {
            selectAvatar(R.drawable.avatar4)
        }
        dialog.show()
    }

    private fun selectAvatar(avatarResId: Int) {
        binding.profileImage.setImageResource(avatarResId)
        saveAvatarToDatabase(avatarResId)
        dialog.dismiss()
    }

    private fun saveAvatarToDatabase(avatarResId: Int) {
        val currentUserUid = auth.currentUser?.uid ?: return
        val avatarName = when (avatarResId) {
            R.drawable.avatar1 -> "avatar1"
            R.drawable.avatar2 -> "avatar2"
            R.drawable.avatar3 -> "avatar3"
            R.drawable.avatar4 -> "avatar4"
            else -> "default_avatar"
        }

        databaseReference.child(currentUserUid).child("Imagen").child("imag").setValue(avatarName)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Avatar successfully updated", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Avatar updated successfully: $avatarName")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to update avatar: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to update avatar", exception)
            }
    }


    private fun getUserData(uid: String) {
        databaseReference.child(uid).child("Profile").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    binding.edUsername.setText(it.username)
                    binding.edEmail.setText(it.email)
                    binding.edNivell.setText(it.nivell)


                }
                databaseReference.child(uid).child("Profile").child("familyUser").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val familyUser: String? =  snapshot.getValue(String::class.java)
                        binding.edUserFamiliar.setText(familyUser)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
                    }
                })

                databaseReference.child(uid).child("Profile").child("familyEmail").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val familyEmail: String? =  snapshot.getValue(String::class.java)
                        binding.edEmailFamiliar.setText(familyEmail)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }

    fun updatePassword(newPassword: String) {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Password updated successfully.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Password update failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "No authenticated user.", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}