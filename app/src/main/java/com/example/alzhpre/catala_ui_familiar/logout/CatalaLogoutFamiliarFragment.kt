package com.example.alzhpre.catala_ui_familiar.logout

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.alzhpre.CatalaSignInActivity
import com.example.alzhpre.databinding.CatalaFragmentLogoutBinding


class CatalaLogoutFamiliarFragment : Fragment() {

    private var _binding: CatalaFragmentLogoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(activity, CatalaSignInActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
