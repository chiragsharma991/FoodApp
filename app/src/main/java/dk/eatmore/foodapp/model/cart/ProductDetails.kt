package dk.eatmore.foodapp.model.cart

import android.databinding.BindingAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import bolts.Bolts
import com.google.gson.annotations.SerializedName
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.model.ModelUtility
import java.io.Serializable


data class ProductDetails(
        val msg: String = "",
        val data: Data,
        val productImagePath: String = "",
        val productImageThumbnailPath: String = "",
        val status: Boolean = false) : ModelUtility(),Serializable


data class Data(
        val featured: String = "",
        val restaurantId: String = "",
        val pDesc: String = "",
        val p_price: String = "",
        val productNo: String = "",
        val pName: String = "",
        val is_attributes: String = "",
        val p_id: String = "",
        val extra_topping_group_deatils: Extra_topping_group_deatils  = Extra_topping_group_deatils("","","","","","","", arrayListOf()),
        val product_ingredients: ArrayList<ProductIngredientsItem> = arrayListOf(),
        val product_attribute_list: ArrayList<ProductAttributeListItem> = arrayListOf()
) : Serializable


//-----------

data class ProductIngredientsItem(
        val i_id: String = "",
        val restaurant_id: String = "",
        val actionDt: String = "",
        var selected_ingredient :Boolean = true,
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
        val product_attribute_value: ArrayList<ProductAttributeValueItem>? = arrayListOf(),
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
        var is_itemselected: Boolean = false,
        val extra_topping_group_deatils: Extra_topping_group_deatils = Extra_topping_group_deatils("","","","","","","", arrayListOf())
) : Serializable







data class Extra_topping_group_deatils(
        val action_by: String = "",
        val action_dt: String = "",
        val is_deleted: String = "",
        val is_activated: String = "",
        val restaurant_id: String = "",
        val tg_name: String = "",
        val tm_id: String = "",
        val topping_subgroup_list: ArrayList<Topping_subgroup_list> = arrayListOf()
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
        val topping_subgroup_details: ArrayList<Topping_subgroup_details> = arrayListOf()
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
        var is_et_itemselected: Boolean = false,
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