package dk.eatmore.foodapp.utils

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.model.cart.ProductAttributeListItem
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.cart.ProductIngredientsItem
import org.json.JSONException
import org.json.JSONObject

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
     */

    fun getjsonparmsofAddtocart(p_id: String, product_ingredients: MutableLiveData<ArrayList<ProductIngredientsItem>>,
                                product_attribute_list: MutableLiveData<ArrayList<ProductAttributeListItem>>, productDetails: ProductDetails, list_is: Int): JsonArray {

        val ingredientArray = JsonArray()
        if (product_ingredients.value !=null && product_ingredients.value!!.size > 0) {
            for (i in 0..product_ingredients.value!!.size - 1) {
                val jObject = JsonObject()
                if (product_ingredients.value!![i].selected_ingredient) {
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


}