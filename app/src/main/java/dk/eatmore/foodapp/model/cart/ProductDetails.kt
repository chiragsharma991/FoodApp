package dk.eatmore.foodapp.model.cart

import com.google.gson.annotations.SerializedName


data class ProductDetails(
        val msg: String = "",
        val data: Data,
        val productImagePath: String = "",
        val productImageThumbnailPath: String = "",
        val status: Boolean = false)


data class Data(
        val featured: String = "",
        val restaurantId: String = "",
        val pDesc: String = "",
        val pPrice: String = "",
        val productNo: String = "",
        val pName: String = "",
        val isAttributes: String = "",
        val pId: String = "",
        val product_ingredients: ArrayList<ProductIngredientsItem>?,
        val product_attribute_list: ArrayList<ProductAttributeListItem>?)


data class ProductIngredientsItem(
        val i_id: String = "",
        val restaurant_id: String = "",
        val actionDt: String = "",
        val i_name: String = "")


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
        val product_attribute_value: ArrayList<ProductAttributeValueItem>?,
        val default_attribute_value: DefaultAttributeValue)

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
        val actionDt: String = "")

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
        val actionDt: String = "")