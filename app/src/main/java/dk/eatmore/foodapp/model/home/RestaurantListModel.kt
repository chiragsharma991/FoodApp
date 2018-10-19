package dk.eatmore.foodapp.model.home

import android.databinding.BindingAdapter
import android.view.View
import com.google.gson.annotations.SerializedName
import dk.eatmore.foodapp.utils.Constants
import java.io.Serializable

data class RestaurantListModel(
                               val msg: String = "",
                               val restaurant_list: RestaurantList,
                               val area_details: AreaDetails,
                               val status: Boolean = false):Serializable

data class RestaurantList(
        val open_now: ArrayList<Restaurant> = arrayListOf(),
        val pre_order: ArrayList<Restaurant> = arrayListOf(),
        val closed: ArrayList<Restaurant> = arrayListOf()
):Serializable

data class AreaDetails(val city_name: String = "", val postal_code: String = ""):Serializable





data class Restaurant(
        val postal_code: String = "",
        val restaurant_name: String = "",
        val address: String = "",
        val city: String = "",
        val cuisines: String = "",
        val total_rating: Float = 0.0f,
        val quality_of_food_ratiing: Float = 0.0f,
        val customer_service_ratiing: Float = 0.0f,
        val delivery_time_ratiing: Float = 0.0f,
        val app_icon: String = "",
        val r_key : String="",
        val r_token : String="",
        val cartcnt : String?=null,
        val free_text : String="",
        val shipping_type : String="",
        val shipping_unit : String="",
        val rating_details : ArrayList<Rating_details> = arrayListOf(),
        val review_list : ArrayList<Review_list> = arrayListOf(),
        val opening_hours : ArrayList<Opening_hours> = arrayListOf(),
        val shipping_charges : ArrayList<Shipping_charges> = arrayListOf(),
        val total_review_count : String=""

):Serializable


data class Rating_details(val review: Int = 0, val review_count: Int = 0, val rate_per: Int = 0):Serializable
data class Review_list(val order_date: String = "", val review_by: String = "", val review: String = "",val rating : String=""):Serializable
data class Opening_hours(val today: Boolean = false, val opens: String = "", val day: String = "",val closes : String=""):Serializable
data class Shipping_charges(

        val minimum_order_price : String="",
        val postal_code : String="",
        val from_pd : String="",
        val price : String="",
        val to_pd : String=""



):Serializable