package dk.eatmore.foodapp.activity.main.filter

import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.utils.BaseFragment
import java.util.ArrayList

abstract class SearchRestaurant : BaseFragment() {

    abstract fun searchcompleted(list: RestaurantListModel)
    lateinit var searched_restaurantlistmodel: RestaurantListModel


    val TAG: String = "SearchRestaurant"


    protected fun searchRestaurantList(value: String, filterable_restaurantlistmodel: RestaurantListModel?) {

        synchronized(this){

            val char = value.trim().toLowerCase()
            if (char.length <= 0) {
                // Add all list
                loge(TAG, "Add all list--")
                searched_restaurantlistmodel = filterable_restaurantlistmodel!!
                searchcompleted(searched_restaurantlistmodel)

            } else {
                // add list according to target
                loge(TAG, "Add list according to target--")

                val open_now: ArrayList<Restaurant> = ArrayList()
                val pre_order: ArrayList<Restaurant> = ArrayList()
                val closed: ArrayList<Restaurant> = ArrayList()
                var restaurant_name = ""
                var cuisines_name = ""

                for (i in 0 until filterable_restaurantlistmodel!!.restaurant_list.open_now.size) {
                    restaurant_name = filterable_restaurantlistmodel.restaurant_list.open_now[i].restaurant_name.trim().toLowerCase()
                    cuisines_name = filterable_restaurantlistmodel.restaurant_list.open_now[i].cuisines.trim().toLowerCase()
                    if (restaurant_name.contains(char) || cuisines_name.contains(char)) {
                        open_now.add(filterable_restaurantlistmodel.restaurant_list.open_now.get(i))
                    }
                }

                for (j in 0 until filterable_restaurantlistmodel.restaurant_list.pre_order.size) {
                    restaurant_name = filterable_restaurantlistmodel.restaurant_list.pre_order[j].restaurant_name.trim().toLowerCase()
                    cuisines_name = filterable_restaurantlistmodel.restaurant_list.pre_order[j].cuisines.trim().toLowerCase()
                    if (restaurant_name.contains(char) || cuisines_name.contains(char)) {
                        pre_order.add(filterable_restaurantlistmodel.restaurant_list.pre_order.get(j))
                    }
                }

                for (k in 0 until filterable_restaurantlistmodel.restaurant_list.closed.size) {
                    restaurant_name = filterable_restaurantlistmodel.restaurant_list.closed[k].restaurant_name.trim().toLowerCase()
                    cuisines_name = filterable_restaurantlistmodel.restaurant_list.closed[k].cuisines.trim().toLowerCase()
                    if (restaurant_name.contains(char) || cuisines_name.contains(char)) {
                        closed.add(filterable_restaurantlistmodel.restaurant_list.closed.get(k))
                    }


                }

                searched_restaurantlistmodel = filterable_restaurantlistmodel.copy(restaurant_list = dk.eatmore.foodapp.model.home.RestaurantList(open_now = open_now, pre_order = pre_order, closed = closed))
                searchcompleted(searched_restaurantlistmodel)
            }

        }

    }
}