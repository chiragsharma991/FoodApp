package dk.eatmore.foodapp.activity.main.filter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.utils.BaseActivity
import dk.eatmore.foodapp.utils.Constants
import java.util.*

abstract class Kokken_tilpas_filter : BaseActivity() {

    private lateinit var filterable_restaurantlistmodel: RestaurantListModel
    private val TAG = "Kokken_tilpas_filter"


    fun filterbyKokken(kokkenType_list: ArrayList<RestaurantList.kokken_Model>, easysort_list: ArrayList<RestaurantList.kokken_Model>, tilpassort_list: ArrayList<RestaurantList.kokken_Model>, restaurantlistmodel: RestaurantListModel) {

        // Note: if checkedItem list is [""] empty then it will add all list  just like select All option.
        // $contains -> specific object $conainsAll -> collection type like model class
        val checkedItem: ArrayList<String> = ArrayList()
        for (kokken_Model_ in kokkenType_list) {
            if (kokken_Model_.is_itemselected && !(kokken_Model_.itemtype.trim() == getString(R.string.all).trim())) {
                checkedItem.add(kokken_Model_.itemtype.trim())
            }
        }
        val open_now: ArrayList<Restaurant> = ArrayList()
        val pre_order: ArrayList<Restaurant> = ArrayList()
        val closed: ArrayList<Restaurant> = ArrayList()

        for (i in 0 until restaurantlistmodel.restaurant_list.open_now.size) {
            loge(KokkenType.TAG, "check-" + restaurantlistmodel.restaurant_list.open_now[i].cuisines_list.toString() + "-" + checkedItem.toString() + "-")
            if(checkedItem.size <= 0){
                // if All is select
                open_now.add(restaurantlistmodel.restaurant_list.open_now.get(i))
            }else{
                for (j in 0 until checkedItem.size ){
                    if (restaurantlistmodel.restaurant_list.open_now[i].cuisines_list.contains(checkedItem[j])) {
                        open_now.add(restaurantlistmodel.restaurant_list.open_now.get(i))
                        break
                    }
                }
            }


        }
        for (i in 0 until restaurantlistmodel.restaurant_list.pre_order.size) {
            if(checkedItem.size <= 0){
                // if All is select
                pre_order.add(restaurantlistmodel.restaurant_list.pre_order.get(i))
            }else{
                for (j in 0 until checkedItem.size ){
                    if (restaurantlistmodel.restaurant_list.pre_order[i].cuisines_list.contains(checkedItem[j])) {
                        pre_order.add(restaurantlistmodel.restaurant_list.pre_order.get(i))
                        break
                    }
                }
            }
        }
        for (i in 0 until restaurantlistmodel.restaurant_list.closed.size) {
            if(checkedItem.size <= 0){
                // if All is select
                closed.add(restaurantlistmodel.restaurant_list.closed.get(i))
            }else{
                for (j in 0 until checkedItem.size ){
                    if (restaurantlistmodel.restaurant_list.closed[i].cuisines_list.contains(checkedItem[j])) {
                        closed.add(restaurantlistmodel.restaurant_list.closed.get(i))
                        break
                    }
                }
            }
        }


        filterbytilpas(easysort_list, tilpassort_list, restaurantlistmodel.copy(restaurant_list = dk.eatmore.foodapp.model.home.RestaurantList(open_now = open_now, pre_order = pre_order, closed = closed)))


    }


    private fun filterbytilpas(easysort_list: ArrayList<RestaurantList.kokken_Model>, tilpassort_list: ArrayList<RestaurantList.kokken_Model>, restaurantlistmodel: RestaurantListModel) {


        val open_now: ArrayList<Restaurant> = ArrayList()
        val pre_order: ArrayList<Restaurant> = ArrayList()
        val closed: ArrayList<Restaurant> = ArrayList()

        // Open now--
        for (i in 0 until restaurantlistmodel.restaurant_list.open_now.size) {
            val restaurant = restaurantlistmodel.restaurant_list.open_now[i]

            if (tilpassort_list[0].is_itemselected) {
                // Gratis levering
                if (!restaurant.sort_free_delivery) {
                    // dont add this list
                    continue
                }

            }

            if (tilpassort_list[1].is_itemselected) {
                // 5 + Rate
                if (!restaurant.sort_fiveplus_rate) {
                    // dont add this list
                    continue
                }

            }

            if (tilpassort_list[2].is_itemselected) {
                // open now
                if (!restaurant.is_open_now) {
                    // dont add this list
                    continue
                }

            }
            if (tilpassort_list[3].is_itemselected) {
                // pickup
                if (!restaurant.pickup_present) {
                    // dont add this list
                    continue
                }

            }
            if (tilpassort_list[4].is_itemselected) {
                // new restaurant
                if (restaurant.is_new == "0") {
                    // dont add this list
                    continue
                }

            }

            // if all sorting checks are disable or matched any item the add.
            open_now.add(restaurant)

        }

        // pre order--
        for (i in 0 until restaurantlistmodel.restaurant_list.pre_order.size) {
            val restaurant = restaurantlistmodel.restaurant_list.pre_order[i]

            if (tilpassort_list[0].is_itemselected) {
                // Gratis levering
                if (!restaurant.sort_free_delivery) {
                    // dont add this list
                    continue
                }
            }

            if (tilpassort_list[1].is_itemselected) {
                // 5 + Rate
                if (!restaurant.sort_fiveplus_rate) {
                    // dont add this list
                    continue
                }
            }

            if (tilpassort_list[2].is_itemselected) {
                // open now
                if (!restaurant.is_open_now) {
                    // dont add this list
                    continue
                }
            }

            if (tilpassort_list[3].is_itemselected) {
                // pickup
                if (!restaurant.pickup_present) {
                    // dont add this list
                    continue
                }

            }
            if (tilpassort_list[4].is_itemselected) {
                // new restaurant
                if (restaurant.is_new == "0") {
                    // dont add this list
                    continue
                }

            }

            // if all sorting checks are disable or matched any item the add.
            pre_order.add(restaurant)

        }

        // closed order--
        for (i in 0 until restaurantlistmodel.restaurant_list.closed.size) {
            val restaurant = restaurantlistmodel.restaurant_list.closed[i]

            if (tilpassort_list[0].is_itemselected) {
                // Gratis levering
                if (!restaurant.sort_free_delivery) {
                    // dont add this list
                    continue
                }
            }

            if (tilpassort_list[1].is_itemselected) {
                // 5 + Rate
                if (!restaurant.sort_fiveplus_rate) {
                    // dont add this list
                    continue
                }
            }

            if (tilpassort_list[2].is_itemselected) {
                // open now
                if (!restaurant.is_open_now) {
                    // dont add this list
                    continue
                }

            }
            if (tilpassort_list[3].is_itemselected) {
                // pickup
                if (!restaurant.pickup_present) {
                    // dont add this list
                    continue
                }

            }
            if (tilpassort_list[4].is_itemselected) {
                // new restaurant
                if (restaurant.is_new == "0") {
                    // dont add this list
                    continue
                }

            }

            // if all sorting checks are disable or matched any item the add.
            closed.add(restaurant)

        }


        sortbytilpas(easysort_list, tilpassort_list, restaurantlistmodel.copy(restaurant_list = dk.eatmore.foodapp.model.home.RestaurantList(open_now = open_now, pre_order = pre_order, closed = closed)))


    }


    private fun sortbytilpas(easysort_list: ArrayList<RestaurantList.kokken_Model>, tilpassort_list: ArrayList<RestaurantList.kokken_Model>, restaurantlistmodel: RestaurantListModel) {


        val open_now: ArrayList<Restaurant> = ArrayList()
        val pre_order: ArrayList<Restaurant> = ArrayList()
        val closed: ArrayList<Restaurant> = ArrayList()

        // Popular sort--
        if (easysort_list[0].is_itemselected) {

            // Open now (12-5-2)
            Collections.sort(restaurantlistmodel.restaurant_list.open_now, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.total_rating == rhs.total_rating){
                        return 0
                    } else if(lhs.total_rating > rhs.total_rating){
                        return -1
                    }else{
                        return 1
                    }
                }
            })

            // Pre order
            Collections.sort(restaurantlistmodel.restaurant_list.pre_order, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.total_rating == rhs.total_rating){
                        return 0
                    } else if(lhs.total_rating > rhs.total_rating){
                        return -1
                    }else{
                        return 1
                    }
                }
            })

            // closed restaurant
            Collections.sort(restaurantlistmodel.restaurant_list.closed, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.total_rating == rhs.total_rating){
                        return 0
                    } else if(lhs.total_rating > rhs.total_rating){
                        return -1
                    }else{
                        return 1
                    }
                }
            })


            open_now.addAll(restaurantlistmodel.restaurant_list.open_now)
            pre_order.addAll(restaurantlistmodel.restaurant_list.pre_order)
            closed.addAll(restaurantlistmodel.restaurant_list.closed)

        }
        // delivery charge--(5-8-12)
        else if(easysort_list[1].is_itemselected){

            // Open now
            Collections.sort(restaurantlistmodel.restaurant_list.open_now, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.sort_delivery_charge == rhs.sort_delivery_charge){
                        return 0
                    }else if(lhs.sort_delivery_charge > rhs.sort_delivery_charge){
                        return 1
                    }else{
                        return -1
                    }
                }
            })

            // Pre order
            Collections.sort(restaurantlistmodel.restaurant_list.pre_order, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.sort_delivery_charge == rhs.sort_delivery_charge){
                        return 0
                    }else if(lhs.sort_delivery_charge > rhs.sort_delivery_charge){
                        return 1
                    }else{
                        return -1
                    }
                }
            })

            // closed restaurant
            Collections.sort(restaurantlistmodel.restaurant_list.closed, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.sort_delivery_charge == rhs.sort_delivery_charge){
                        return 0
                    }else if(lhs.sort_delivery_charge > rhs.sort_delivery_charge){
                        return 1
                    }else{
                        return -1
                    }
                }
            })


            open_now.addAll(restaurantlistmodel.restaurant_list.open_now)
            pre_order.addAll(restaurantlistmodel.restaurant_list.pre_order)
            closed.addAll(restaurantlistmodel.restaurant_list.closed)

        }
        // Minimum order-- (5-9-12)
        else if(easysort_list[2].is_itemselected){

            // Open now
            Collections.sort(restaurantlistmodel.restaurant_list.open_now, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.sort_min_order_price == rhs.sort_min_order_price){
                        return 0
                    }else if(lhs.sort_min_order_price > rhs.sort_min_order_price){
                        return 1
                    }else{
                        return -1
                    }
                }
            })

            // Pre order
            Collections.sort(restaurantlistmodel.restaurant_list.pre_order, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.sort_min_order_price == rhs.sort_min_order_price){
                        return 0
                    }else if(lhs.sort_min_order_price > rhs.sort_min_order_price){
                        return 1
                    }else{
                        return -1
                    }
                }
            })

            // closed restaurant
            Collections.sort(restaurantlistmodel.restaurant_list.closed, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.sort_min_order_price == rhs.sort_min_order_price){
                        return 0
                    }else if(lhs.sort_min_order_price > rhs.sort_min_order_price){
                        return 1
                    }else{
                        return -1
                    }
                }
            })


            open_now.addAll(restaurantlistmodel.restaurant_list.open_now)
            pre_order.addAll(restaurantlistmodel.restaurant_list.pre_order)
            closed.addAll(restaurantlistmodel.restaurant_list.closed)

        }
        // Newest first (current data according)-- (12-5-2)
        else if(easysort_list[3].is_itemselected){

            // Open now
            Collections.sort(restaurantlistmodel.restaurant_list.open_now, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.sort_created_date > rhs.sort_created_date){
                        return 0
                    }else if(lhs.sort_created_date > rhs.sort_created_date){
                        return -1
                    }else{
                        return 1
                    }
                }
            })

            // Pre order
            Collections.sort(restaurantlistmodel.restaurant_list.pre_order, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.sort_created_date > rhs.sort_created_date){
                        return 0
                    }else if(lhs.sort_created_date > rhs.sort_created_date){
                        return -1
                    }else{
                        return 1
                    }
                }
            })

            // closed restaurant
            Collections.sort(restaurantlistmodel.restaurant_list.closed, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    if(lhs.sort_created_date > rhs.sort_created_date){
                        return 0
                    }else if(lhs.sort_created_date > rhs.sort_created_date){
                        return -1
                    }else{
                        return 1
                    }
                }
            })


            open_now.addAll(restaurantlistmodel.restaurant_list.open_now)
            pre_order.addAll(restaurantlistmodel.restaurant_list.pre_order)
            closed.addAll(restaurantlistmodel.restaurant_list.closed)

        }
        // Name according--
        else if(easysort_list[4].is_itemselected){

            // Open now
            Collections.sort(restaurantlistmodel.restaurant_list.open_now, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                  return  lhs.restaurant_name.compareTo(rhs.restaurant_name)
                }
            })

            // Pre order
            Collections.sort(restaurantlistmodel.restaurant_list.pre_order, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    return  lhs.restaurant_name.compareTo(rhs.restaurant_name)
                }
            })

            // closed restaurant
            Collections.sort(restaurantlistmodel.restaurant_list.closed, object : Comparator<Restaurant> {
                override fun compare(lhs: Restaurant, rhs: Restaurant): Int {

                    return  lhs.restaurant_name.compareTo(rhs.restaurant_name)
                }
            })


            open_now.addAll(restaurantlistmodel.restaurant_list.open_now)
            pre_order.addAll(restaurantlistmodel.restaurant_list.pre_order)
            closed.addAll(restaurantlistmodel.restaurant_list.closed)
        }

        else{
            // if not select any sort params then:
            open_now.addAll(restaurantlistmodel.restaurant_list.open_now)
            pre_order.addAll(restaurantlistmodel.restaurant_list.pre_order)
            closed.addAll(restaurantlistmodel.restaurant_list.closed)
        }

            if(this is KokkenType){
                //kokken
                filterable_restaurantlistmodel=restaurantlistmodel.copy(restaurant_list =dk.eatmore.foodapp.model.home.RestaurantList(open_now = open_now ,pre_order = pre_order,closed = closed) )
                val intent = Intent()
                val bundle = Bundle()
                bundle.putSerializable(Constants.FILTER_RESTAURANTLISTMODEL,filterable_restaurantlistmodel)
                intent.putExtra(Constants.BUNDLE,bundle)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }else{
                //tilpas
                filterable_restaurantlistmodel=restaurantlistmodel.copy(restaurant_list =dk.eatmore.foodapp.model.home.RestaurantList(open_now = open_now ,pre_order = pre_order,closed = closed) )
                val intent = Intent()
                val bundle = Bundle()
                bundle.putSerializable(Constants.FILTER_RESTAURANTLISTMODEL,filterable_restaurantlistmodel)
                intent.putExtra(Constants.BUNDLE,bundle)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }

    }

}