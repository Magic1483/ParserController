package com.example.parsercontroller

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.Serializable


object Messages {
    private val _messages = mutableStateListOf<ConsoleMsg>() // external access
    val messages: List<ConsoleMsg> get() = _messages // internal access only

    fun AddMsg(content:String,type:String){
        _messages.add(ConsoleMsg(content,type))
    }

    fun Clear(){
        _messages.clear()
    }
}