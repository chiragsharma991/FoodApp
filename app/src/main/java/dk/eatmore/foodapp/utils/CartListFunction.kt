package dk.eatmore.foodapp.utils

import android.app.Dialog
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.epay.fragment.Paymentmethod
import dk.eatmore.foodapp.model.cart.ProductAttributeListItem
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.cart.ProductIngredientsItem
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import kotlinx.android.synthetic.main.infodialog.*
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
            postParam.addProperty(Constants.FIRST_TIME, EpayFragment.paymentattributes.first_time)
            postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,"") )
            // postParam.addProperty(Constants.POSTAL_CODE, EpayFragment.paymentattributes.postal_code)
            postParam.addProperty(Constants.DISCOUNT_TYPE, EpayFragment.paymentattributes.discount_type)
            postParam.addProperty(Constants.DISCOUNT_AMOUNT, EpayFragment.paymentattributes.discount_amount)
            postParam.addProperty(Constants.DISCOUNT_ID,EpayFragment.paymentattributes.discount_id)
            postParam.addProperty(Constants.SHIPPING, if (EpayFragment.isPickup) context.getString(R.string.pickup_) else context.getString(R.string.delivery_))
            postParam.addProperty(Constants.TELEPHONE_NO, EpayFragment.paymentattributes.telephone_no)
            postParam.addProperty(Constants.ORDER_TOTAL, EpayFragment.paymentattributes.order_total)
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
            postParam.addProperty(Constants.ACCEPT_TC, "1")
            postParam.addProperty(Constants.PAYMETHOD, if(Paymentmethod.isPaymentonline) "1" else "2" )
            postParam.addProperty(Constants.EXPECTED_TIME, EpayFragment.paymentattributes.expected_time)
            postParam.addProperty(Constants.COMMENTS, EpayFragment.paymentattributes.comments)
            postParam.addProperty(Constants.DEVICE_TYPE,Constants.DEVICE_TYPE_VALUE)
            postParam.addProperty(Constants.FIRST_NAME, EpayFragment.paymentattributes.first_name)
            postParam.addProperty(Constants.ADDITIONAL_CHARGE, if(Paymentmethod.isPaymentonline) EpayFragment.paymentattributes.additional_charges_online else EpayFragment.paymentattributes.additional_charges_cash)
            postParam.addProperty(Constants.LANGUAGE, Constants.DA)
            postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
            val jsonarray=JsonArray()
            for (i in 0.until(EpayFragment.selected_op_id.size) ){
                val jsonobject= JsonObject()
                jsonobject.addProperty(Constants.OP_ID, EpayFragment.selected_op_id.get(i))
                jsonarray.add(jsonobject)
            }
            postParam.add(Constants.CARTPRODUCTS,jsonarray )

            if(EpayFragment.isPickup){
                //pickup--
                checkout_api=ApiCall.checkout_pickup(postParam)
            }else{
                // delivery--
                postParam.addProperty(Constants.ADDRESS, EpayFragment.paymentattributes.address)
                postParam.addProperty(Constants.POSTAL_CODE, EpayFragment.paymentattributes.postal_code)
                postParam.addProperty(Constants.DISTANCE, EpayFragment.paymentattributes.distance)
                postParam.addProperty(Constants.MINIMUM_ORDER_PRICE, EpayFragment.paymentattributes.minimum_order_price)
                postParam.addProperty(Constants.SHIPPING_COSTS, EpayFragment.paymentattributes.shipping_charge)
                postParam.addProperty(Constants.UPTO_MIN_SHIPPING, EpayFragment.paymentattributes.upto_min_shipping)
                postParam.addProperty(Constants.SHIPPING_REMARK, "")
                checkout_api= ApiCall.checkout_delivery(postParam)
            }


        }catch (error : Exception){
            return null
        }

        return checkout_api

    }

    fun showDialog(restaurant: Restaurant, context: Context) {
        val dialog = Dialog(context, R.style.AppCompatAlertDialogStyle_Transparent)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.infodialog)

        val shippinginfo_container = dialog.shippinginfo_container as LinearLayout

        try {

            if (!(restaurant.shipping_charges.size > 0)) {
                // show empty
                return
            }
            shippinginfo_container.removeAllViewsInLayout()
            if (restaurant.shipping_type == "by_distance") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Fra (km)", "Til (km)", "Pris (kr.)")
                // Add header
                for (i in 0..2) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 8
                    else if (i == 2)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms


                    textView1.text = headerlist[i]
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.CENTER_HORIZONTAL
                    else if (i == 2)
                        textView1.gravity = Gravity.END
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.SubtitleMidium_TextViewSmall)
                    //  textView1.typeface= Typeface.DEFAULT_BOLD
                    //  textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                    parent.addView(textView1)

                }

                shippinginfo_container.addView(parent)
                val view = View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin = 8
                view.alpha = 0.3f
                view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                view.layoutParams = vparms
                shippinginfo_container.addView(view)


                // Add values
                for (i in 0 until restaurant.shipping_charges.size) {
                    parent = LinearLayout(context)
                    val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.topMargin = 8
                    parent.layoutParams = parms
                    parent.orientation = LinearLayout.HORIZONTAL

                    // add row
                    for (j in 0..2) {
                        // add column
                        // Add textview 1
                        val textView1 = AppCompatTextView(context)
                        val parms = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        parms.weight = 1f
                        if (j == 0)
                            parms.rightMargin = 8
                        else if (j == 1)
                            parms.rightMargin = 8
                        else if (j == 2)
                            parms.rightMargin = 0
                        textView1.layoutParams = parms

                        if (j == 0)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].from_pd)
                        else if (j == 1)
                            textView1.text = if (restaurant.shipping_charges[i].to_pd == null) Constants.OPEFTER else BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].to_pd!!)
                        else if (j == 2)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].price)

                        if (j == 0)
                            textView1.gravity = Gravity.START
                        else if (j == 1)
                            textView1.gravity = Gravity.CENTER_HORIZONTAL
                        else if (j == 2)
                            textView1.gravity = Gravity.END
                        textView1.setSingleLine(true)
                        textView1.setTextAppearance(context, R.style.Subtitle_TextViewSmall)
                        // textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                        parent.addView(textView1)

                    }
                    shippinginfo_container.addView(parent)
                    val view = View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin = 8
                    view.alpha = 0.3f
                    view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                    view.layoutParams = vparms
                    shippinginfo_container.addView(view)


                }


            } else if (restaurant.shipping_type == "by_postal") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Postnr.", "Min. (kr.)", "Pris (kr.)")
                // Add header
                for (i in 0..2) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 8
                    else if (i == 2)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms


                    textView1.text = headerlist[i]
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.CENTER_HORIZONTAL
                    else if (i == 2)
                        textView1.gravity = Gravity.END
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.SubtitleMidium_TextViewSmall)
                    //  textView1.typeface= Typeface.DEFAULT_BOLD
                    //  textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                    parent.addView(textView1)

                }

                shippinginfo_container.addView(parent)
                val view = View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin = 8
                view.alpha = 0.3f
                view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                view.layoutParams = vparms
                shippinginfo_container.addView(view)


                // Add values
                for (i in 0 until restaurant.shipping_charges.size) {
                    parent = LinearLayout(context)
                    var parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.topMargin = 8
                    parent.layoutParams = parms
                    parent.orientation = LinearLayout.HORIZONTAL

                    // add row
                    for (j in 0..2) {
                        // add column
                        // Add textview 1
                        val textView1 = AppCompatTextView(context)
                        parms = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        parms.weight = 1f
                        if (j == 0)
                            parms.rightMargin = 8
                        else if (j == 1)
                            parms.rightMargin = 8
                        else if (j == 2)
                            parms.rightMargin = 0
                        textView1.layoutParams = parms

                        if (j == 0)
                            textView1.text = restaurant.shipping_charges[i].postal_code
                        else if (j == 1)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].minimum_order_price)
                        else if (j == 2)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].price)

                        if (j == 0)
                            textView1.gravity = Gravity.START
                        else if (j == 1)
                            textView1.gravity = Gravity.CENTER_HORIZONTAL
                        else if (j == 2)
                            textView1.gravity = Gravity.END
                        textView1.setSingleLine(true)
                        textView1.setTextAppearance(context, R.style.Subtitle_TextViewSmall)
                        // textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                        parent.addView(textView1)

                    }
                    shippinginfo_container.addView(parent)
                    val view = View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin = 8
                    view.alpha = 0.3f
                    view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                    view.layoutParams = vparms
                    shippinginfo_container.addView(view)


                }


            } else if (restaurant.shipping_type == "by_order_price") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Fra (Pris)", "Til (Pris.)", "Pris (kr.)")
                // Add header
                for (i in 0..2) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 8
                    else if (i == 2)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms


                    textView1.text = headerlist[i]
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.CENTER_HORIZONTAL
                    else if (i == 2)
                        textView1.gravity = Gravity.END
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.SubtitleMidium_TextViewSmall)
                    //  textView1.typeface= Typeface.DEFAULT_BOLD
                    //  textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                    parent.addView(textView1)

                }

                shippinginfo_container.addView(parent)
                val view = View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin = 8
                view.alpha = 0.3f
                view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                view.layoutParams = vparms
                shippinginfo_container.addView(view)


                // Add values
                for (i in 0 until restaurant.shipping_charges.size) {
                    parent = LinearLayout(context)
                    var parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.topMargin = 8
                    parent.layoutParams = parms
                    parent.orientation = LinearLayout.HORIZONTAL

                    // add row
                    for (j in 0..2) {
                        // add column
                        // Add textview 1
                        val textView1 = AppCompatTextView(context)
                        parms = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        parms.weight = 1f
                        if (j == 0)
                            parms.rightMargin = 8
                        else if (j == 1)
                            parms.rightMargin = 8
                        else if (j == 2)
                            parms.rightMargin = 0
                        textView1.layoutParams = parms

                        if (j == 0)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].from_pd)
                        else if (j == 1)
                            textView1.text = if (restaurant.shipping_charges[i].to_pd == null) Constants.OPEFTER else BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].to_pd!!)
                        else if (j == 2)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].price)

                        if (j == 0)
                            textView1.gravity = Gravity.START
                        else if (j == 1)
                            textView1.gravity = Gravity.CENTER_HORIZONTAL
                        else if (j == 2)
                            textView1.gravity = Gravity.END
                        textView1.setSingleLine(true)
                        textView1.setTextAppearance(context, R.style.Subtitle_TextViewSmall)
                        // textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                        parent.addView(textView1)

                    }
                    shippinginfo_container.addView(parent)
                    val view = View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin = 8
                    view.alpha = 0.3f
                    view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                    view.layoutParams = vparms
                    shippinginfo_container.addView(view)


                }


            } else if (restaurant.shipping_type == "flat_rate") {

                val parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL
                val headerlist = arrayListOf("Pris (Kr.)", BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges.get(0).price))
                // Add header
                for (i in 0..1) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms
                    textView1.text = headerlist[i]
                    textView1.typeface = Typeface.DEFAULT_BOLD
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.CENTER_HORIZONTAL
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.SubtitleMidium_TextViewSmall)
                    // textView1.typeface= Typeface.DEFAULT_BOLD
                    // textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB
                    parent.addView(textView1)
                }

                shippinginfo_container.addView(parent)


            }


        } catch (e: Exception) {
            Log.e("exception", e.message.toString())
        }

        dialog.show()

    }





}