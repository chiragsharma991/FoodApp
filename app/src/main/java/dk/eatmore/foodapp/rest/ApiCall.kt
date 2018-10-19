package dk.eatmore.foodapp.rest

import com.google.gson.JsonObject
import dk.eatmore.foodapp.model.home.ProductListModel
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.model.cart.Data
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.model.home.RestaurantListModel
import retrofit2.Call


class ApiCall {
   // username,password_hash,type,login_type

    companion object {

        private fun getApiInterface(): ApiInterface {
            return ApiClient.getClient()!!.create(ApiInterface::class.java)
        }
        fun myOrder( r_key: String, r_token: String) : Call<LastOrder> {
            return getApiInterface().myOrder(r_key,r_token)

        }


        fun fBlogin(jsonObject: JsonObject ) : Call<JsonObject> {
            return getApiInterface().fBlogin(jsonObject)
        }

        fun login(jsonObject: JsonObject ) : Call<JsonObject> {
            return getApiInterface().login(jsonObject)
        }

        fun signup(jsonObject: JsonObject ) : Call<JsonObject> {
            return getApiInterface().signup(jsonObject )
        }


        fun getProductList(r_token : String, r_key : String, customer_id : String ) : Call<ProductListModel> {
            return getApiInterface().setProductList(r_token, r_key, customer_id)
        }


        fun getProductDetails(r_token : String , r_key : String, p_id : String ) : Call<ProductDetails> {
            return getApiInterface().setProductDetails(r_token, r_key, p_id)
        }

        fun restaurantList(jsonObject: JsonObject ) : Call<RestaurantListModel> {
            return getApiInterface().restaurantList(jsonObject)
        }


        fun getPickuptime(r_token : String , r_key : String, shipping : String, language : String ) : Call<JsonObject> {
            return getApiInterface().getPickuptime(r_token, r_key, shipping,language)
        }

        fun addtocart(jsonObject: JsonObject) : Call<JsonObject> {
            return getApiInterface().addtocart(jsonObject)
        }

        fun viewcart(jsonObject: JsonObject) : Call<ViewcardModel> {
            return getApiInterface().viewcart(jsonObject)
        }

        fun deleteitemFromcart(jsonObject: JsonObject) : Call<JsonObject> {
            return getApiInterface().deleteitemFromcart(jsonObject)
        }

        fun openingHours(r_token : String , r_key : String) : Call<JsonObject> {
            return getApiInterface().openingHours(r_token, r_key)
        }

        fun lastLogin(r_token : String , r_key : String , customer_id : String , device_type: String ) : Call<JsonObject> {
            return getApiInterface().lastLogin(r_token, r_key,customer_id,device_type)
        }


    }




}