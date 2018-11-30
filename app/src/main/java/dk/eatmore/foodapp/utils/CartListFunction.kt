package dk.eatmore.foodapp.utils

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.epay.fragment.Paymentmethod
import dk.eatmore.foodapp.model.cart.ProductAttributeListItem
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.cart.ProductIngredientsItem
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call

object CartListFunction {


    fun calculateValuesofAddtocart(product_attribute_list: MutableLiveData<ArrayList<ProductAttributeListItem>>, productDetails: ProductDetails): Double {

        var attribute_cost: Double = 0.0



        if (product_attribute_list.value !=null && product_attribute_list.value!!.size > 0) {

            // if attributes and extratoppings are present...

            for (i in 0..product_attribute_list.value!!.size - 1) {

                for (j in 0..product_attribute_list.value!![i].product_attribute_value!!.size - 1) {

                    if (product_attribute_list.value!![i].product_attribute_value!![j].is_itemselected) {
                        attribute_cost = attribute_cost + product_attribute_list.value!![i].product_attribute_value!![j].a_price.toDouble()
//
                        for (k in 0..product_attribute_list.value!![i].product_attribute_value!![j].extra_topping_group_deatils.topping_subgroup_list.size - 1) {

                            for (l in 0..product_attribute_list.value!![i].product_attribute_value!![j].extra_topping_group_deatils.topping_subgroup_list[k].topping_subgroup_details.size - 1) {

                                if (product_attribute_list.value!![i].product_attribute_value!![j].extra_topping_group_deatils.topping_subgroup_list[k].topping_subgroup_details[l].is_et_itemselected) {
                                    attribute_cost = attribute_cost + product_attribute_list.value!![i].product_attribute_value!![j].extra_topping_group_deatils.topping_subgroup_list[k].topping_subgroup_details[l].t_price.toDouble()
                                }
                            }
                        }
                    }
                }
            }
        } else {

            // if only extratoppings are present...
            attribute_cost= productDetails.data.p_price.toDouble()
            for (i in 0..productDetails.data.extra_topping_group_deatils.topping_subgroup_list.size - 1) {

                for (j in 0..productDetails.data.extra_topping_group_deatils.topping_subgroup_list[i].topping_subgroup_details.size - 1) {
                    if(productDetails.data.extra_topping_group_deatils.topping_subgroup_list[i].topping_subgroup_details.get(j).is_et_itemselected){
                        attribute_cost = attribute_cost + productDetails.data.extra_topping_group_deatils.topping_subgroup_list[i].topping_subgroup_details.get(j).t_price.toDouble()
                    }
                }

            }
        }


        Log.e("TAG", "total cost: " + attribute_cost)
        return attribute_cost
    }


    /**
     * @param list_is : what list you want to get.
     *
     */

    fun getjsonparmsofAddtocart(p_id: String, product_ingredients: MutableLiveData<ArrayList<ProductIngredientsItem>>,
                                product_attribute_list: MutableLiveData<ArrayList<ProductAttributeListItem>>, productDetails: ProductDetails, list_is: Int): JsonArray {

// Note: in ingredients we are submiting those contain which are selected (GREEN colour) so end of the screen API give result of opposite like : -pizz -categhd (this is are removed contain)
        val ingredientArray = JsonArray()
        if (product_ingredients.value !=null && product_ingredients.value!!.size > 0) {
            for (i in 0..product_ingredients.value!!.size - 1) {
                val jObject = JsonObject()
                if (!product_ingredients.value!![i].selected_ingredient) {
                    jObject.addProperty("i_id", product_ingredients.value!![i].i_id)
                    jObject.addProperty("p_id", p_id)
                    ingredientArray.add(jObject)
                }
            }
        }
        Log.e("TAG", "ingredientArray >" + ingredientArray.toString())

        val attributeArray = JsonArray()
        val extratoppingArray = JsonArray()

        if (product_attribute_list.value !=null && product_attribute_list.value!!.size > 0) {
            // attributes + extratoppings
            for (i in 0..product_attribute_list.value!!.size - 1) {
                for (j in 0..product_attribute_list.value!!.get(i).product_attribute_value!!.size - 1) {

                    if (product_attribute_list.value!!.get(i).product_attribute_value!!.get(j).is_itemselected) {
                        val jObject = JsonObject()
                        jObject.addProperty("p_id", p_id)
                        jObject.addProperty("pad_id", product_attribute_list.value!!.get(i).product_attribute_value!!.get(j).pad_id)
                        attributeArray.add(jObject)

                        for (k in 0..product_attribute_list.value!!.get(i).product_attribute_value!!.get(j).extra_topping_group_deatils.topping_subgroup_list.size - 1) {

                            for (l in 0..product_attribute_list.value!!.get(i).product_attribute_value!!.get(j).extra_topping_group_deatils.topping_subgroup_list.get(k).topping_subgroup_details.size - 1) {

                                if (product_attribute_list.value!!.get(i).product_attribute_value!!.get(j).extra_topping_group_deatils.topping_subgroup_list.get(k).topping_subgroup_details.get(l).is_et_itemselected) {
                                    val jObject = JsonObject()
                                    jObject.addProperty("p_id", p_id)
                                    jObject.addProperty("pad_id", product_attribute_list.value!!.get(i).product_attribute_value!!.get(j).pad_id)
                                    jObject.addProperty("tsgd_id", product_attribute_list.value!!.get(i).product_attribute_value!!.get(j).extra_topping_group_deatils.topping_subgroup_list.get(k).topping_subgroup_details.get(l).tsgd_id)
                                    extratoppingArray.add(jObject)
                                }
                            }
                        }
                    }

                }
            }

        }else{
            // attributes + onlyextratoppings
            val onlyextratoppingArray = JsonArray()

            for (i in 0..productDetails.data.extra_topping_group_deatils.topping_subgroup_list.size - 1) {

                for (j in 0..productDetails.data.extra_topping_group_deatils.topping_subgroup_list[i].topping_subgroup_details.size - 1) {
                    if(productDetails.data.extra_topping_group_deatils.topping_subgroup_list[i].topping_subgroup_details.get(j).is_et_itemselected){
                        val jObject = JsonObject()
                        jObject.addProperty("p_id", p_id)
                        jObject.addProperty("tsgd_id", productDetails.data.extra_topping_group_deatils.topping_subgroup_list[i].topping_subgroup_details.get(j).tsgd_id)
                        onlyextratoppingArray.add(jObject)
                    }
                }

            }
            if(extratoppingArray.size() <= 0){
                extratoppingArray.addAll(onlyextratoppingArray)
            }
        }






        Log.e("TAG", "attributeArray >" + attributeArray.toString())
        Log.e("TAG", "extratoppingArray >" + extratoppingArray.toString())


        return if (list_is == 0) ingredientArray else if (list_is == 1) attributeArray else extratoppingArray


    }


  /*  "status": true,
         "is_user_deleted": false,
    "is_restaurant_closed": false,
    "paymethod": "Online Payment",
    "order_total": 31.34,
    "msg": "Checkout Successfully",
    "epay_merchant": "6673007",
    "order_no": 95,
    "pre_order": true*/


     fun getcartpaymentAttributes (context : Context) : Call<JsonObject>? {
        val checkout_api : Call<JsonObject>

        val postParam = JsonObject()
        try {
            postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
            postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
            postParam.addProperty(Constants.FIRST_TIME, EpayActivity.paymentattributes.first_time)
            postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,"") )
           // postParam.addProperty(Constants.POSTAL_CODE, EpayActivity.paymentattributes.postal_code)
            postParam.addProperty(Constants.DISCOUNT_TYPE, EpayActivity.paymentattributes.discount_type)
            postParam.addProperty(Constants.DISCOUNT_AMOUNT, EpayActivity.paymentattributes.discount_amount)
            postParam.addProperty(Constants.DISCOUNT_ID,EpayActivity.paymentattributes.discount_id)
            postParam.addProperty(Constants.SHIPPING, if (EpayActivity.isPickup) context.getString(R.string.pickup) else context.getString(R.string.delivery))
            postParam.addProperty(Constants.TELEPHONE_NO, EpayActivity.paymentattributes.telephone_no)
            postParam.addProperty(Constants.ORDER_TOTAL, EpayActivity.paymentattributes.order_total)
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
            postParam.addProperty(Constants.ACCEPT_TC, "1")
            postParam.addProperty(Constants.PAYMETHOD, if(Paymentmethod.isPaymentonline) "1" else "2" )
            postParam.addProperty(Constants.EXPECTED_TIME, EpayActivity.paymentattributes.expected_time)
            postParam.addProperty(Constants.COMMENTS, EpayActivity.paymentattributes.comments)
            postParam.addProperty(Constants.DEVICE_TYPE,Constants.DEVICE_TYPE_VALUE)
            postParam.addProperty(Constants.FIRST_NAME, EpayActivity.paymentattributes.first_name)
            postParam.addProperty(Constants.ADDITIONAL_CHARGE, if(Paymentmethod.isPaymentonline) EpayActivity.paymentattributes.additional_charges_online else EpayActivity.paymentattributes.additional_charges_cash)
            postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
            val jsonarray=JsonArray()
            for (i in 0.until(EpayActivity.selected_op_id.size) ){
                val jsonobject= JsonObject()
                jsonobject.addProperty(Constants.OP_ID, EpayActivity.selected_op_id.get(i))
                jsonarray.add(jsonobject)
            }
            postParam.add(Constants.CARTPRODUCTS,jsonarray )

            if(EpayActivity.isPickup){
                //pickup--
                checkout_api=ApiCall.checkout_pickup(postParam)
            }else{
                // delivery--
                postParam.addProperty(Constants.ADDRESS, EpayActivity.paymentattributes.address)
                postParam.addProperty(Constants.POSTAL_CODE, EpayActivity.paymentattributes.postal_code)
                postParam.addProperty(Constants.DISTANCE, EpayActivity.paymentattributes.distance)
                postParam.addProperty(Constants.MINIMUM_ORDER_PRICE, EpayActivity.paymentattributes.minimum_order_price)
                postParam.addProperty(Constants.SHIPPING_COSTS, EpayActivity.paymentattributes.shipping_charge)
                postParam.addProperty(Constants.UPTO_MIN_SHIPPING, EpayActivity.paymentattributes.upto_min_shipping)
                postParam.addProperty(Constants.SHIPPING_REMARK, "")
                postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
                checkout_api= ApiCall.checkout_delivery(postParam)
            }


        }catch (error : Exception){
            return null
        }

        return checkout_api

    }




}