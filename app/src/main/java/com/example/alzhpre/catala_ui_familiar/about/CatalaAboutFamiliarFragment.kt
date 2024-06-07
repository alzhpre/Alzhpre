package com.example.alzhpre.catala_ui_familiar.about

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
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
import com.example.alzhpre.R
import com.example.alzhpre.data.FamilyUser

import com.example.alzhpre.databinding.CatalaFragmentAboutFamiliarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class CatalaAboutFamiliarFragment : Fragment() {

    private var _binding: CatalaFragmentAboutFamiliarBinding? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReferenceFamily: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var dialog: Dialog
    private val PICK_IMAGE_REQUEST = 1
    private val handler = Handler(Looper.getMainLooper())

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(com.example.p1prova.catala_ui_familiar.about.CatalaAboutViewModel::class.java)
        _binding = CatalaFragmentAboutFamiliarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("FamilyUsers")
        databaseReferenceFamily = FirebaseDatabase.getInstance().getReference("FamilyUsers")

        updateAvatar()


        handler.postDelayed(object : Runnable {
            override fun run() {
                updateAvatar()
                handler.postDelayed(this, 5000)
            }
        }, 5000)


        uid?.let { getUserData(it)}
        binding.profileImage.setOnClickListener {
            showAvatarSelectionDialog()
        }


        binding.saveBtn.setOnClickListener {
            showProgressBar()
            val username = binding.edUsername.text.toString()
            val email = binding.edEmail.text.toString()
            val pass = binding.edPassword.text.toString()
            val familyUser = binding.edUserFamiliar.text.toString()
            val user = FamilyUser(username, email, familyUser)

            if (uid != null) {
                databaseReference.child(uid).setValue(user).addOnSuccessListener {
                    updateAvatar()
                }.addOnFailureListener {
                    hideProgressBar()
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }

            if (pass.isNotEmpty() && isValidPassword(pass)) {
                updatePassword(pass)
            }
        }
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
                Log.d(ContentValues.TAG, "Avatar updated successfully: $avatarName")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to update avatar: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e(ContentValues.TAG, "Failed to update avatar", exception)
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

    private fun getUserData(uid: String) {
        databaseReferenceFamily.child(uid).child("Profile").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(FamilyUser::class.java)
                user?.let {
                    binding.edUsername.setText(it.username)
                    binding.edEmail.setText(it.email)
                    binding.edUserFamiliar.setText(it.familyUser)
                }
            } else {
                Log.d(ContentValues.TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "get failed with ", exception)
        }
    }

    private fun showProgressBar() {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
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

    private fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}