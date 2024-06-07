package com.example.p1prova.catala_ui_familiar.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CatalaAboutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is about us"
    }
    val text: LiveData<String> = _text
}