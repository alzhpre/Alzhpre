package com.example.alzhpre.catala_ui_familiar.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CatalaGraphViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "STILL IN DEVELOPMENT"
    }
    val text: LiveData<String> = _text
}