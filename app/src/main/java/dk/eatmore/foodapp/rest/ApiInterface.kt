package dk.eatmore.foodapp.rest

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface ApiInterface {



    @FormUrlEncoded
    @POST("User/user/login")
    fun setLogin(
            @Field("username") username: String,
            @Field("password_hash") password_hash: String,
            @Field("type") type: String,
            @Field("login_type") login_type: String,
            @Field("language") language: String
    ): Call<JsonObject>



}