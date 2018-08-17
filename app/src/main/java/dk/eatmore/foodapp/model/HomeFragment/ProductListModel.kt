package dk.eatmore.foodapp.model.HomeFragment

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ProductListModel(val msg: String = "",
                            val cartcnt: String = "",
                            val is_user_deleted: Boolean = false,
                            val image_path: String = "",
                            val product_image_thumbnail_path: String = "",
                            val time: String = "",
                            val menu: ArrayList<MenuListItem>? = null,
                            val status: Boolean = false)

data class MenuListItem(val c_desc: String = "",
                    val c_order: String = "",
                    val restaurant_id: String = "",
                    val c_id: String = "",
                    val c_name: String = "",
                    val product_list: ArrayList<ProductListItem>? = null): Serializable


data class ProductListItem(val productIngredients: String = "",
                           val restaurantId: String = "",
                           val productAttribute: ArrayList<ProductAttributeItem>? = null,
                           val p_desc: String = "",
                           val p_price: String = "",
                           val productNo: String = "",
                           val p_name: String = "",
                           val extraToppingGroup: String = "",
                           val cId: String = "",
                           val isAttributes: String = "",
                           val pImage: String = "",
                           val pId: String = "")


data class ProductAttributeItem(val pamId: String = "",
                                val aName: String = "",
                                val defaultAttributeValue: DefaultAttributeValue)


data class DefaultAttributeValue(val padId: String = "",
                                 val aPrice: String = "",
                                 val aValue: String = "")