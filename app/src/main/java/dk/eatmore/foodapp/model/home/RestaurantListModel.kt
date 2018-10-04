package dk.eatmore.foodapp.model.home

import com.google.gson.annotations.SerializedName

data class RestaurantListModel(
                               val msg: String = "",
                               val restaurant_list: RestaurantList,
                               val area_details: AreaDetails,
                               val status: Boolean = false)

data class RestaurantList(
        val open_now: ArrayList<Restaurant> = arrayListOf(),
        val pre_order: ArrayList<Restaurant> = arrayListOf(),
        val closed: ArrayList<Restaurant> = arrayListOf()
)

data class AreaDetails(val city_name: String = "", val postal_code: String = "")



data class Restaurant(
        val postal_code: String = "",
        val restaurant_name: String = "",
        val address: String = "",
        val restaurant_rating: Double = 0.0

)


data class Rating_details(val review: Int = 0, val reviewCount: Int = 0, val ratePer: Int = 0)
data class Review_list(val review: Int = 0, val reviewCount: Int = 0, val ratePer: Int = 0)