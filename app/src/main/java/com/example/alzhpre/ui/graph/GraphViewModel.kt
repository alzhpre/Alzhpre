package com.example.alzhpre.ui.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GraphViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "STILL IN DEVELOPMENT"
    }
    val text: LiveData<String> = _text
}