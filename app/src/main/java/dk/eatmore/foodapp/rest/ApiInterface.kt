package dk.eatmore.foodapp.rest

import com.google.gson.JsonObject
import dk.eatmore.foodapp.model.home.ProductListModel
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.model.cart.Data
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.epay.ViewcardModel
import retrofit2.Call
import retrofit2.http.*


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
    @POST("Enduser/enduser/create_record")
    fun signup(
            @Field("username") username: String ,
            @Field("password_hash") password_hash: String,
            @Field("postal_code") postal_code: String,
            @Field("house_no") house_no: String,
            @Field("telephone_no") telephone_no: String,
            @Field("first_name") first_name: String,
            @Field("email") email: String,
            @Field("r_token") r_token: String,
            @Field("r_key") r_key: String
    ): Call<JsonObject>



    @FormUrlEncoded
    @POST("Category/category/menu")
    fun setProductList(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("customer_id") customer_id: String
    ): Call<ProductListModel>



    @FormUrlEncoded
    @POST("Product/productmaster/product-details")
    fun setProductDetails(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("p_id") p_id: String
    ): Call<ProductDetails>

    @FormUrlEncoded
    @POST("Cart/cart/restpickupdeltime")
    fun getPickuptime(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("shipping") shipping: String,
            @Field("language") language: String
    ): Call<JsonObject>


    @POST("Cart/cart/addtocart")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun addtocart(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/viewcart")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun viewcart(@Body jsonObject: JsonObject): Call<ViewcardModel>


}