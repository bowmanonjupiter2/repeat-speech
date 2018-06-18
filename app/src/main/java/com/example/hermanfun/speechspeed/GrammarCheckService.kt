package com.example.hermanfun.speechspeed

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.*

interface GrammarCheckService {
    companion object {
        val BASE_URL : String
        get() = "https://languagetool.org/api/"
    }

    @GET("v2/languages")
    fun getAvailableLanguages():Observable<SupportedLanguages>

    @POST("v2/check")
    fun checkGrammar()
}