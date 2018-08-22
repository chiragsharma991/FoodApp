package dk.eatmore.foodapp.model.cart

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class ProductDetails(
        val msg: String = "",
        val data: Data,
        val productImagePath: String = "",
        val productImageThumbnailPath: String = "",
        val status: Boolean = false) : Serializable


data class Data(
        val featured: String = "",
        val restaurantId: String = "",
       val pDesc: String = "",
        val pPrice: String = "",
        val productNo: String = "",
        val pName: String = "",
       val isAttributes: String = "",
        val pId: String = "",
        val product_ingredients: ArrayList<ProductIngredientsItem>? =null,
        val product_attribute_list: ArrayList<ProductAttributeListItem>?=null
) : Serializable


//-----------

data class ProductIngredientsItem(
        val i_id: String = "",
        val restaurant_id: String = "",
        val actionDt: String = "",
        val i_name: String = "") : Serializable



data class ProductAttributeListItem(
        val display_type: String = "",
        val action_by: String = "",
        val is_deleted: String = "",
        val is_activated: String = "",
        val pam_id: String = "",
        val restaurant_id: String = "",
        val action_dt: String = "",
        val a_name: String = "",
        val p_id: String = "",
        val product_attribute_value: ArrayList<ProductAttributeValueItem>?=null,
        val default_attribute_value: DefaultAttributeValue) : Serializable

//------------------

data class ProductAttributeValueItem(
        val actionBy: String = "",
        val pad_id: String = "",
        val a_price: String = "",
        val isDeleted: String = "",
        val tmId: String = "",
        val isActivated: String = "",
        val pamId: String = "",
        val a_value: String = "",
        val restaurant_id: String = "",
        val actionDt: String = "",
        val extra_topping_group_deatils: Extra_topping_group_deatils
) : Serializable

data class Extra_topping_group_deatils(
        val action_by: String = "",
        val action_dt: String = "",
        val is_deleted: String = "",
        val is_activated: String = "",
        val restaurant_id: String = "",
        val tg_name: String = "",
        val tm_id: String = "",
        val topping_subgroup_list: ArrayList<Topping_subgroup_list>? =null
) : Serializable

data class Topping_subgroup_list(
        val action_by: String = "",
        val action_dt: String = "",
        val is_deleted: String = "",
        val is_activated: String = "",
        val restaurant_id: String = "",
        val tm_id: String = "",
        val tsg_name: String = "",
        val tsg_id: String = "",
        val topping_subgroup_details: ArrayList<Topping_subgroup_details>?= null
) : Serializable


data class Topping_subgroup_details(
        val action_by: String = "",
        val action_dt: String = "",
        val is_deleted: String = "",
        val is_activated: String = "",
        val restaurant_id: String = "",
        val i_id: String = "",
        val i_name: String = "",
        val pad_id: String = "",
        val tsgd_id: String = "",
        val tsg_id: String = "",
        val t_price: String = "") : Serializable


//-------------------


data class DefaultAttributeValue(
        val actionBy: String = "",
        val pad_id: String = "",
        val aPrice: String = "",
        val isDeleted: String = "",
        val tmId: String = "",
        val isActivated: String = "",
        val pamId: String = "",
        val aValue: String = "",
        val restaurantId: String = "",
        val actionDt: String = "") : Serializable