package dk.eatmore.foodapp.rest

import com.google.gson.JsonObject
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.storage.PreferenceUtil
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

        fun login(username : String, password_hash : String, type : String,login_type : String) : Call<JsonObject> {
            return getApiInterface().setLogin(username, password_hash, type,login_type, PreferenceUtil.getString(PreferenceUtil.LANGUAGE,"")!!)
        }


    }




}