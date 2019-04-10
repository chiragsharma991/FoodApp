package dk.eatmore.foodapp.rest

import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.EditAddress
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.RestPaymentMethods
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedDetails
import dk.eatmore.foodapp.model.home.ProductListModel
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.model.cart.Data
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.epay.ApplyCodeModel
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.utils.Constants
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {


    @FormUrlEncoded
    @POST("http://eatmoredev.dk/restapi/v2/OrderRejectTemplate/order-reject-template/all_record")
    fun myOrder(
            @Field("r_key") r_key: String,
            @Field("r_token") r_token : String,
            @Field("app") app : String

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
            @Field("customer_id") customer_id: String,
            @Field("app") app : String
    ): Call<ProductListModel>

    @FormUrlEncoded
    @POST("Enduser/enduser/forgot-password")
    fun forgot_password(
            @Field("auth_key") auth_key: String ,
            @Field("eatmore_app") eatmore_app: Boolean,
            @Field("device_type") device_type: String,
            @Field("email") email : String,
            @Field("app") app : String
    ): Call<JsonObject>


    @FormUrlEncoded
    @POST("Category/category/menu")
    fun restaurant_info(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("customer_id") customer_id: String,
            @Field("app") app : String
    ): Call<OrderFragment.Myorder_Model>

    @FormUrlEncoded
    @POST("Product/productmaster/product-details")
    fun setProductDetails(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("p_id") p_id: String,
            @Field("app") app : String
    ): Call<ProductDetails>


    @FormUrlEncoded
    @POST("Openinghours/openinghours/all_record")
    fun openingHours(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("app") app : String
    ): Call<JsonObject>

    @FormUrlEncoded
    @POST("Enduser/enduser/device-token")
    fun devicetoken(
            @Field("token") token: String,
            @Field("device_type") device_type: String,
            @Field("auth_key") auth_key: String,
            @Field("eatmore_app") eatmore_app: Boolean,
            @Field("user_id") user_id: String,
            @Field("app") app : String
    ): Call<JsonObject>

    //        fun change_password ( old_password : String, newpassword : String,language : String) : Call<JsonObject> {
    @FormUrlEncoded
    @POST("Enduser/enduser/change-password")
    fun change_password(
            @Field("device_type") device_type: String,
            @Field("auth_key") auth_key: String,
            @Field("eatmore_app") eatmore_app: Boolean,
            @Field("id") id: String,
            @Field("old_password") old_password: String,
            @Field("newpassword") newpassword: String,
            @Field("language") language: String,
            @Field("app") app : String
    ): Call<JsonObject>

    @FormUrlEncoded
    @POST("Cart/cart/clearcart")
    fun clearcart(
            @Field(Constants.AUTH_KEY) auth_key: String,
            @Field(Constants.EATMORE_APP) eatmore_app: Boolean,
            @Field(Constants.CUSTOMER_ID) customer_id: String,
            @Field(Constants.APP) app : String
    ): Call<JsonObject>


    @FormUrlEncoded
    @POST("Enduser/enduser/last-login")
    fun lastLogin(
            @Field(Constants.AUTH_KEY) auth_key: String ,
            @Field(Constants.CUSTOMER_ID) customer_id: String,
            @Field(Constants.DEVICE_TYPE) device_type: String,
            @Field(Constants.EATMORE_APP) eatmore_app: Boolean,
            @Field(Constants.APP) app : String
    ): Call<JsonObject>

    @POST("Cart/cart/apply-code")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun applycode(@Body jsonObject: JsonObject): Call<ApplyCodeModel>

    @POST("Cart/cart/user-info")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun userInfo(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("RestaurantPaymentMethod/restaurant-payment-method/view_record")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun restaurant_payment_method(@Body jsonObject: JsonObject): Call<RestPaymentMethods.RestPayMethodModel>

    @POST("Category/category/menu")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun category_menu(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/shipping-address-list")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun shippingaddress_list(@Body jsonObject: JsonObject): Call<EditAddress.EditaddressListModel>

    @POST("Enduser/enduser/my-orders")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun myorders(@Body jsonObject: JsonObject): Call<OrderFragment.Myorder_Model>

    @POST("Enduser/enduser/delete-shipping-address")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun delete_shippingaddress(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/edit-shipping-address")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun edit_shippingaddress(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/add-shipping-address")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun add_shippingaddress(@Body jsonObject: JsonObject): Call<JsonObject>

    @FormUrlEncoded
    @POST("Enduser/enduser/update_record")
    fun update_record(
            @Field("auth_key") auth_key: String ,
            @Field("eatmore_app") eatmore_app: Boolean,
            @Field("id") id: String,
            @Field("first_name") first_name: String,
            @Field("email") email: String,
            @Field("telephone_no") telephone_no: String,
            @Field("app") app : String,
            @Field("subscribe") subscribe : Int
    ): Call<JsonObject>

    @POST("Cart/cart/delivery-details")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun deliveryDetails(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/delete_record")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun delete_record(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/pickup-info")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun pickupinfo(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/checkout-delivery")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun checkout_delivery(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("PosOrder/order/order-details")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun orderdetails(@Body jsonObject: JsonObject): Call<OrderedDetails>

    @POST("PosOrder/order/re-order")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun reorder(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/last-order")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun lastorder(@Body jsonObject: JsonObject): Call<OrderFragment.Myorder_Model>

    @POST("Enduser/enduser/rating")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun rating(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/app-rating")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun app_rating(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/checkout-pickup")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun checkout_pickup(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Cart/cart/ordertransaction")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun ordertransaction(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/check-order")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun check_order(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/add-favorite-restaurant")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun add_favorite_restaurant(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/remove-favorite-restaurant")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun remove_favorite_restaurant(@Body jsonObject: JsonObject): Call<JsonObject>

    @POST("Enduser/enduser/force-update")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun force_update (@Body jsonObject: JsonObject): Call<JsonObject>

    @FormUrlEncoded
    @POST("Cart/cart/cancelordertransaction")
    fun cancelordertransaction(
            @Field("r_token") r_token: String ,
            @Field("r_key") r_key: String,
            @Field("order_no") order_no: Int,
            @Field("app") app : String
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

    @POST("Enduser/enduser/restaurant-closed")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun restaurant_closed(@Body jsonObject: JsonObject): Call<JsonObject>


}