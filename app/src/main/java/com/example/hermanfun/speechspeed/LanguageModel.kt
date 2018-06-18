package com.example.hermanfun.speechspeed


data class SupportedLanguages(var list:Array<LanguageModel>){
    data class LanguageModel(var name:String, var code:String, var longCode:String) {
    }
}