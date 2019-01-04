package dk.eatmore.foodapp.rest

import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.EditAddress
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedDetails
import dk.eatmore.foodapp.model.home.ProductListModel
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.model.cart.Data
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.utils.Constants
import retrofit2.Call


class ApiCall {
   // username,password_hash,type,login_type

    companion object {

        private fun getApiInterface(): ApiInterface {
            return ApiClient.getClient()!!.create(ApiInterface::class.java)
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


      /*  fun getProductList(r_token : String, r_key : String, customer_id : String ) : Call<ProductListModel> {
            return getApiInterface().setProductList(r_token, r_key, customer_id,Constants.RESTAURANT_FOOD_ANDROID)
        }*/

        fun category_menu (jsonObject: JsonObject ) : Call<JsonObject> {
            return getApiInterface().category_menu(jsonObject)
        }

     /*   fun restaurant_info(r_token : String, r_key : String, customer_id : String ) : Call<OrderFragment.Myorder_Model> {
            return getApiInterface().restaurant_info(r_token, r_key, customer_id,Constants.RESTAURANT_FOOD_ANDROID)
        }*/

        fun getProductDetails(r_token : String , r_key : String, p_id : String ) : Call<ProductDetails> {
            return getApiInterface().setProductDetails(r_token, r_key, p_id,Constants.RESTAURANT_FOOD_ANDROID)
        }

        fun restaurantList(jsonObject: JsonObject ) : Call<RestaurantListModel> {
            return getApiInterface().restaurantList(jsonObject)
        }

        fun pickupinfo(jsonObject: JsonObject) : Call<JsonObject> {
            return getApiInterface().pickupinfo(jsonObject)
        }
        fun checkout_delivery(jsonObject: JsonObject) : Call<JsonObject> {
            return getApiInterface().checkout_delivery(jsonObject)
        }
        fun orderdetails(jsonObject: JsonObject) : Call<OrderedDetails> {
            return getApiInterface().orderdetails(jsonObject)
        }
        fun reorder(jsonObject: JsonObject) : Call<JsonObject> {
            return getApiInterface().reorder(jsonObject)
        }
        fun rating(jsonObject: JsonObject) : Call<JsonObject> {
            return getApiInterface().rating(jsonObject)
        }
        fun lastorder(jsonObject: JsonObject) : Call<OrderFragment.Myorder_Model> {
            return getApiInterface().lastorder(jsonObject)
        }
        fun checkout_pickup(jsonObject: JsonObject) : Call<JsonObject> {
            return getApiInterface().checkout_pickup(jsonObject)
        }
        fun ordertransaction(jsonObject: JsonObject) : Call<JsonObject> {
            return getApiInterface().ordertransaction(jsonObject)
        }
        fun cancelordertransaction(r_token : String , r_key : String, order_no : Int ) : Call<JsonObject> {
            return getApiInterface().cancelordertransaction(r_token,r_key,order_no,Constants.RESTAURANT_FOOD_ANDROID)
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

        fun restaurant_closed(jsonObject: JsonObject) : Call<JsonObject> {
            return getApiInterface().restaurant_closed(jsonObject)
        }

        fun openingHours(r_token : String , r_key : String) : Call<JsonObject> {
            return getApiInterface().openingHours(r_token, r_key,Constants.RESTAURANT_FOOD_ANDROID)
        }
        fun devicetoken (token : String , eatmore_app : Boolean, auth_key : String, device_type : String, user_id: String) : Call<JsonObject> {
            return getApiInterface().devicetoken(token=token ,eatmore_app = eatmore_app, auth_key =auth_key, device_type = device_type, user_id = user_id,app = Constants.RESTAURANT_FOOD_ANDROID )
        }
        fun clearcart (eatmore_app : Boolean, auth_key : String, customer_id : String) : Call<JsonObject> {
            return getApiInterface().clearcart(eatmore_app = eatmore_app, auth_key =auth_key, customer_id = customer_id,app = Constants.RESTAURANT_FOOD_ANDROID )
        }
        fun lastLogin(auth_key : String  , customer_id : String , device_type: String,eatmore_app : Boolean ) : Call<JsonObject> {
            return getApiInterface().lastLogin(auth_key = auth_key, eatmore_app =eatmore_app, device_type = device_type, customer_id = customer_id,app = Constants.RESTAURANT_FOOD_ANDROID)
        }
        fun applycode(r_token : String , r_key : String , customer_id : String , upto_min_shipping: String , shipping : String , code: String , order_total: String , additional_charge: String , shipping_costs: String ) : Call<JsonObject> {
            return getApiInterface().applycode(r_token, r_key,customer_id , upto_min_shipping ,shipping , code, order_total , additional_charge , shipping_costs,Constants.RESTAURANT_FOOD_ANDROID)
        }

        fun userInfo(jsonObject: JsonObject ) : Call<JsonObject> {
            return getApiInterface().userInfo(jsonObject)
        }
        fun shippingaddress_list(jsonObject: JsonObject ) : Call<EditAddress.EditaddressListModel> {
            return getApiInterface().shippingaddress_list(jsonObject)
        }
        fun myorders(jsonObject: JsonObject ) : Call<OrderFragment.Myorder_Model> {
            return getApiInterface().myorders(jsonObject)
        }
        fun delete_shippingaddress(jsonObject: JsonObject ) : Call<JsonObject> {
            return getApiInterface().delete_shippingaddress(jsonObject)
        }
        fun edit_shippingaddress(jsonObject: JsonObject ) : Call<JsonObject> {
            return getApiInterface().edit_shippingaddress(jsonObject)
        }
        fun add_shippingaddress(jsonObject: JsonObject ) : Call<JsonObject> {
            return getApiInterface().add_shippingaddress(jsonObject)
        }
        fun update_record(auth_key : String, telephone_no : String, first_name : String, eatmore_app : Boolean, email : String, id : String) : Call<JsonObject> {
            return getApiInterface().update_record(auth_key =auth_key, telephone_no = telephone_no, first_name = first_name, eatmore_app = eatmore_app, email = email, id = id,app = Constants.RESTAURANT_FOOD_ANDROID )
        }
        fun deliveryDetails(jsonObject: JsonObject ) : Call<JsonObject> {
            return getApiInterface().deliveryDetails(jsonObject)
        }

    }




}