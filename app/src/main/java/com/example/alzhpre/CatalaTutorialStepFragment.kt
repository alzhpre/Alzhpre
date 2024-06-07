package com.example.alzhpre

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar


class CatalaTutorialStepFragment : DialogFragment() {

    private var stepNumber: Int = 1

    companion object {
        private const val ARG_STEP_NUMBER = "step_number"

        fun newInstance(stepNumber: Int): CatalaTutorialStepFragment {
            val fragment = CatalaTutorialStepFragment()
            val args = Bundle()
            args.putInt(ARG_STEP_NUMBER, stepNumber)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            stepNumber = it.getInt(ARG_STEP_NUMBER)
        }
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = when (stepNumber) {
            1 -> R.layout.fragment_tutorial1
            2 -> R.layout.fragment_tutorial2
            3 -> R.layout.fragment_tutorial3
            4 -> R.layout.fragment_tutorial4
            5 -> R.layout.fragment_tutorial5
            6 -> R.layout.fragment_tutorial6
            7 -> R.layout.fragment_tutorial7
            else -> R.layout.catala_activity_pantalla_principal
        }

        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton: Button = view.findViewById(R.id.next_button)

        nextButton.setOnClickListener {
            if (stepNumber < 7) {
                showNextStep(stepNumber + 1)
            } else {

                dismiss() // Cerrar el tutorial al finalizar el último paso
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun showNextStep(stepNumber: Int) {
        val nextStepFragment = newInstance(stepNumber)
        nextStepFragment.show(parentFragmentManager, "tutorial_step_$stepNumber")
        dismiss()
    }

}