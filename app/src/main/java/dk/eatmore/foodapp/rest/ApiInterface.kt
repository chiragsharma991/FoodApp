package dk.eatmore.foodapp.rest

import com.google.gson.JsonObject
import dk.eatmore.foodapp.model.HomeFragment.ProductListModel
import dk.eatmore.foodapp.model.LastOrder
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface ApiInterface {


    @FormUrlEncoded
    @POST("http://eatmoredev.dk/restapi/v2/OrderRejectTemplate/order-reject-template/all_record")
    fun myOrder(
            @Field("r_key") r_key: String,
            @Field("r_token") r_token : String

    ): Call<LastOrder>


    @FormUrlEncoded
    @POST("Enduser/enduser/login")
    fun setLogin(
            @Field("username") username: String ,
            @Field("password_hash") password_hash: String,
            @Field("device_type") device_type: String,
            @Field("is_facebook") is_facebook: String,
            @Field("is_google") is_google: String,
            @Field("language") language: String,
            @Field("r_token") r_token: String,
            @Field("r_key") r_key: String
    ): Call<JsonObject>



    @FormUrlEncoded
    @POST("Category/category/menu")
    fun setProductList(
            @Field("r_token") username: String ,
            @Field("r_key") password_hash: String,
            @Field("customer_id") type: String
    ): Call<ProductListModel>


}