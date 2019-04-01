package dk.eatmore.foodapp.model.home

import dk.eatmore.foodapp.model.ModelUtility
import java.io.Serializable

data class ProductListModel(val msg: String = "",
                            val cartcnt: String = "",
                            val is_user_deleted: Boolean = false,
                            val image_path: String = "",
                            val product_image_thumbnail_path: String = "",
                            val time: String = "",
                            val menu: ArrayList<MenuListItem>? = null,
                            val status: Boolean = false) : ModelUtility(),Serializable

data class MenuListItem(val c_desc: String = "",
                    val c_order: String = "",
                    val restaurant_id: String = "",
                    val c_id: String = "",
                    val c_name: String = "",
                    val product_list: ArrayList<ProductListItem>? = null) :Serializable


data class ProductListItem(val productIngredients: String = "",
                           val restaurantId: String = "",
                           val product_attribute: ArrayList<ProductAttributeItem>? = arrayListOf(),
                           val p_desc: String = "",
                           var actual_price: String ?=null,
                           var actual_price_afterDiscount: String ?=null, // first calculate and added discount into it.
                           var discountType: Int =0,  // 0 no any discount | 1: product discount | 2: order discount
                           val p_price: String ?=null,
                           val productNo: String = "",
                           val p_name: String = "",
                           val extra_topping_group: String? =null,
                           val c_id: String = "",
                           val is_attributes: String = "",
                           val pImage: String = "",
                           val p_id: String = "") :Serializable{

}


data class ProductAttributeItem(val pam_id: String = "",
                                val a_name: String = "",
                                val default_attribute_value: DefaultAttributeValue) :Serializable


data class DefaultAttributeValue(val pad_id: String = "",
                                 val a_price: String = "",
                                 val a_value: String = "") :Serializable


