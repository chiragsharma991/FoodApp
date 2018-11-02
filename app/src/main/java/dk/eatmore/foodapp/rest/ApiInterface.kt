package dk.eatmore.foodapp.rest

import com.google.gson.JsonObject
import dk.eatmore.foodapp.model.home.ProductListModel
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.model.cart.Data
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.model.home.RestaurantListModel
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {


    @FormUrlEncoded
    @POST("http://eatmoredev.dk/restapi/v2/OrderRejectTemplate/order-reject-template/all_record")
    fun myOrder(
            @Field("r_key") r_key: String,
            @Field("r_token") r_token : String

    ): Call<LastOrder>


    @POST("Enduser/enduser/sign-up")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun signup(@Body jsonObject: JsonObject): Call<JsonObject>


    @POST("Enduser/enduser/login")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun fBlogin(@Body jsonObject: JsonObject): Call<JsonObject>


    @POST("Enduser/enduser/login")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun login(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/restaurant-list")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun restaurantList(@Body jsonObject: JsonObject): Call<RestaurantListModel>


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
    @POST("Openinghours/openinghours/all_record")
    fun openingHours(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String
    ): Call<JsonObject>


    @FormUrlEncoded
    @POST("Enduser/enduser/last-login")
    fun lastLogin(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("customer_id") customer_id: String,
            @Field("device_type") device_type: String
    ): Call<JsonObject>

    @FormUrlEncoded
    @POST("Cart/cart/apply-code")
    fun applycode(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("customer_id") customer_id: String,
            @Field("upto_min_shipping") upto_min_shipping: String,
            @Field("shipping") shipping: String,
            @Field("code") code: String,
            @Field("order_total") order_total: String,
            @Field("additional_charge") additional_charge: String,
            @Field("shipping_costs") shipping_costs: String
    ): Call<JsonObject>

    @POST("Cart/cart/user-info")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun userInfo(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/delivery-details")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun deliveryDetails(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/pickup-info")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun pickupinfo(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/checkout-delivery")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun checkout_delivery(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/checkout-pickup")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun checkout_pickup(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/ordertransaction")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun ordertransaction(@Body jsonObject: JsonObject): Call<JsonObject>

    @FormUrlEncoded
    @POST("Cart/cart/cancelordertransaction")
    fun cancelordertransaction(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("order_no") order_no: Int
    ): Call<JsonObject>

    @POST("Cart/cart/addtocart")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun addtocart(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/viewcart")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun viewcart(@Body jsonObject: JsonObject): Call<ViewcardModel>


    @POST("Cart/cart/deletefromcart")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun deleteitemFromcart(@Body jsonObject: JsonObject): Call<JsonObject>


}