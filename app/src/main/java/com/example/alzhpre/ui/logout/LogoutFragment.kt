package com.example.alzhpre.ui.logout

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.alzhpre.databinding.FragmentLogoutBinding
import com.example.alzhpre.SignInActivity



class LogoutFragment : Fragment() {

    private var _binding: FragmentLogoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(activity, SignInActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
