package dk.eatmore.foodapp.model.home

import java.io.Serializable

data class ProductListModel(val msg: String = "",
                            val cartcnt: String = "",
                            val is_user_deleted: Boolean = false,
                            val image_path: String = "",
                            val product_image_thumbnail_path: String = "",
                            val time: String = "",
                            val menu: ArrayList<MenuListItem>? = null,
                            val status: Boolean = false) : Serializable

data class MenuListItem(val c_desc: String = "",
                    val c_order: String = "",
                    val restaurant_id: String = "",
                    val c_id: String = "",
                    val c_name: String = "",
                    val product_list: ArrayList<ProductListItem>? = null) :Serializable


data class ProductListItem(val productIngredients: String = "",
                           val restaurantId: String = "",
                           val product_attribute: ArrayList<ProductAttributeItem> = arrayListOf(),
                           val p_desc: String = "",
                           val p_price: String = "",
                           val productNo: String = "",
                           val p_name: String = "",
                           val extraToppingGroup: String = "",
                           val cId: String = "",
                           val isAttributes: String = "",
                           val pImage: String = "",
                           val p_id: String = "") :Serializable{

}


data class ProductAttributeItem(val pam_id: String = "",
                                val a_name: String = "",
                                val default_attribute_value: DefaultAttributeValue) :Serializable


data class DefaultAttributeValue(val pad_id: String = "",
                                 val a_price: String = "",
                                 val a_value: String = "") :Serializable