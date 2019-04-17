package dk.eatmore.foodapp.model.epay

import com.google.gson.annotations.SerializedName
import dk.eatmore.foodapp.model.ModelUtility
import java.io.Serializable

data class ViewcardModel(
        val msg: String? = null,
        val cartcnt: String = "",
        val is_restaurant_closed: Boolean? = null,
        val pre_order: Boolean? = null,
        val order_total: Double = 0.0,
        val status: Boolean = false,
        val result: ArrayList<ResultItem>?,
        var offer_details : Offer_details?=null

) : ModelUtility()

data class ApplyCodeModel(
        val status: Boolean = false,
        val msg: String? = "",
        val order_total: Double,
        val discount_amount: Double?=0.0,
        val discount_type: String?="",
        val discount_id: String?="",
        val result: ArrayList<ResultItem>?

) : ModelUtility() , Serializable

data class ResultItem(
                      val order_no: String = "",
                      val quantity: String = "",
                      val restaurantId: String = "",
                      val productImage: String = "",
                      val ip: String = "",
                      val op_id: String = "",
                      val discount: String = "",
                      var actual_price: String ?=null,
                      val p_price: String? = null,
                      val product_name: String = "",
                      val product_no: String = "",
                      val pDate: String = "",
                      val cId: String = "",
                      val is_attributes: String? = null,
                      val productImageThumbnail: String = "",
                      val removed_ingredients: List<RemovedIngredientsItem>? = null,
                      val ordered_product_attributes: List<OrderedProductAttributesItem>?= null,
                      val order_product_extra_topping_group: List<OrderProductExtraToppingGroupItem>?= null,
                      val customerId: String = "",
                      val p_id: String = "") : Serializable


data class RemovedIngredientsItem(
                                  val i_id: String = "",
                                  val restaurantId: String = "",
                                  val ingredient_name: String = "",
                                  val opi_id: String = "",
                                  val op_id: String = "",
                                  val customer_id: String = "") : Serializable


data class OrderedProductAttributesItem(
                                        val order_product_extra_topping_group: List<OrderProductExtraToppingGroupItem>?,
                                        val pad_id: String = "",
                                        val a_price: String = "",
                                        val tm_id: String = "",
                                        val restaurant_id: String = "",
                                        val opId: String = "",
                                        val attributeName: String = "",
                                        val opaId: String = "",
                                        val customerId: String = "",
                                        val attribute_value_name: String = ""): Serializable


data class OrderProductExtraToppingGroupItem(
                                             val t_price: String = "",
                                             val tsgd_id: String = "",
                                             val restaurant_id: String = "",
                                             val ingredient_name: String = "",
                                             val opId: String = "",
                                             val op_id: String = "",
                                             val customer_id: String = "",
                                             val opt_id: String = ""): Serializable

data class Offer_details(

        val category_id : ArrayList<String> = arrayListOf(),
        val discount : String ?="",
        val shipping : String?="",
        val offer_type : String?="",
        val minimum_order_price : String?="",
        val offer_id : String?="",
        val message : String?=""

)


