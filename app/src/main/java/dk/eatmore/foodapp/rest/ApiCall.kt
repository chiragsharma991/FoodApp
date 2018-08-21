package dk.eatmore.foodapp.rest

import com.google.gson.JsonObject
import dk.eatmore.foodapp.model.home.ProductListModel
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.model.cart.Data
import dk.eatmore.foodapp.model.cart.ProductDetails
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

        fun login(username : String, password_hash : String, device_type : String , is_facebook : String, is_google:String, r_key:String, r_token : String, language :String ) : Call<JsonObject> {
            return getApiInterface().setLogin(username, password_hash, device_type,is_facebook, is_google, language,r_token,r_key )
        }

        fun Signup(username : String, password_hash : String, postal_code : String , house_no : String, telephone_no:String, r_key:String, r_token : String, first_name :String ,email: String ) : Call<JsonObject> {
            return getApiInterface().signup(username, password_hash, postal_code,house_no, telephone_no, first_name,email,r_token,r_key )
        }


        fun getProductList(r_token : String, r_key : String, customer_id : String ) : Call<ProductListModel> {
            return getApiInterface().setProductList(r_token, r_key, customer_id)
        }


        fun getProductDetails(r_token : String , r_key : String, p_id : String ) : Call<ProductDetails> {
            return getApiInterface().setProductDetails(r_token, r_key, p_id)
        }


    }




}