package dk.eatmore.foodapp.activity.main.filter

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.SelectAddress
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.ActivityTilpasBinding
import dk.eatmore.foodapp.databinding.RowEasySortBinding
import dk.eatmore.foodapp.databinding.RowKokkentypeBinding
import dk.eatmore.foodapp.databinding.RowTilpasSortBinding
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.utils.BaseActivity
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.activity_tilpas.*
import kotlinx.android.synthetic.main.select_address.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.ArrayList


class Tilpas : BaseActivity() {


    private lateinit var binding: ActivityTilpasBinding
    private lateinit var restaurantlistmodel: RestaurantListModel // total selected restaurant from restaurant list (which is still filtered from kokken type)
    private lateinit var filterable_restaurantlistmodel: RestaurantListModel


    private lateinit var tilpassort_mAdapter: UniversalAdapter<RestaurantList.kokken_Model, RowTilpasSortBinding>
    private lateinit var easysort_mAdapter: UniversalAdapter<RestaurantList.kokken_Model, RowEasySortBinding>

    private lateinit var easysort_list: ArrayList<RestaurantList.kokken_Model>
    private lateinit var tilpassort_list: ArrayList<RestaurantList.kokken_Model>

    private val myclickhandler : MyClickHandler =MyClickHandler(this)




    companion object {
        val TAG = "Tilpas"
        fun newInstance(): Tilpas {
            return Tilpas()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_tilpas)
        initView(savedInstanceState)

    }


    private fun initView(savedInstanceState: Bundle?) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        img_toolbar_back.setImageResource(R.drawable.close)
        img_toolbar_back.setOnClickListener{finish()}
        txt_toolbar.setText(getString(R.string.tilpas))
        binding.handler=myclickhandler
        restaurantlistmodel  = intent.getBundleExtra(Constants.BUNDLE).getSerializable(Constants.TILPAS_RESTAURANTLISTMODEL) as RestaurantListModel
        easysort_list =RestaurantList.ui_model!!.easysort_list
        tilpassort_list =RestaurantList.ui_model!!.tilpassort_list
        refreshview()

    }

    private fun refreshview(){

        loge(SelectAddress.TAG,"refresh view...")
        tilpassort_mAdapter = UniversalAdapter(this,tilpassort_list, R.layout.row_tilpas_sort, object : RecyclerCallback<RowTilpasSortBinding, RestaurantList.kokken_Model> {
            override fun bindData(binder: RowTilpasSortBinding, model: RestaurantList.kokken_Model) {
                binder.kokkenModel=model
                binder.handler=myclickhandler
            }
        })
        tilpas_recycler_view.layoutManager = LinearLayoutManager(this)
        tilpas_recycler_view.adapter = tilpassort_mAdapter

        // =====   ======   ==== //

        easysort_mAdapter = UniversalAdapter(this,easysort_list, R.layout.row_easy_sort, object : RecyclerCallback<RowEasySortBinding, RestaurantList.kokken_Model> {
            override fun bindData(binder: RowEasySortBinding, model: RestaurantList.kokken_Model) {
                binder.kokkenModel=model
                binder.handler=myclickhandler
            }
        })
        sort_recycler_view.layoutManager = LinearLayoutManager(this)
        sort_recycler_view.adapter = easysort_mAdapter
    }

    private fun filterbytilpas(){


        val open_now: ArrayList<Restaurant> = ArrayList()
        val pre_order: ArrayList<Restaurant> = ArrayList()
        val closed: ArrayList<Restaurant> = ArrayList()

        // Open now--
        for (i in 0 until restaurantlistmodel.restaurant_list.open_now.size){
            val restaurant =restaurantlistmodel.restaurant_list.open_now[i]

            if(tilpassort_list[0].is_itemselected){
                // Gratis levering
                if(restaurant.sort_free_delivery){
                    // Add this list
                    //open_now.add(restaurant)
                }
                else{
                    // dont add this list
                    continue
                }
            }

            if(tilpassort_list[1].is_itemselected){
                // 5 + Rate
                if(restaurant.sort_fiveplus_rate){
                    // Add this list
                  //  open_now.add(restaurant)
                }
                else{
                    // dont add this list
                    continue
                }
            }

            // if all sorting checks are disable.
            open_now.add(restaurant)

        }

        // pre order--
        for (i in 0 until restaurantlistmodel.restaurant_list.pre_order.size){
            val restaurant =restaurantlistmodel.restaurant_list.pre_order[i]

            if(tilpassort_list[0].is_itemselected){
                // Gratis levering
                if(restaurant.sort_free_delivery){
                    // Add this list
                   // pre_order.add(restaurant)
                }
                else{
                    // dont add this list
                    continue
                }
            }

            if(tilpassort_list[1].is_itemselected){
                // 5 + Rate
                if(restaurant.sort_fiveplus_rate){
                    // Add this list
                 //   pre_order.add(restaurant)
                }
                else{
                    // dont add this list
                    continue
                }
            }

            // if all sorting checks are disable.
            pre_order.add(restaurant)

        }

        // closed order--
        for (i in 0 until restaurantlistmodel.restaurant_list.closed.size){
            val restaurant =restaurantlistmodel.restaurant_list.closed[i]

            if(tilpassort_list[0].is_itemselected){
                // Gratis levering
                if(restaurant.sort_free_delivery){
                    // Add this list
                   // closed.add(restaurant)
                }
                else{
                    // dont add this list
                    continue
                }
            }

            if(tilpassort_list[1].is_itemselected){
                // 5 + Rate
                if(restaurant.sort_fiveplus_rate){
                    // Add this list
                 //   closed.add(restaurant)
                }
                else{
                    // dont add this list
                    continue
                }
            }

            // if all sorting checks are disable.
            closed.add(restaurant)

        }

        filterable_restaurantlistmodel=restaurantlistmodel.copy(restaurant_list =dk.eatmore.foodapp.model.home.RestaurantList(open_now = open_now ,pre_order = pre_order,closed = closed) )
        val intent = Intent()
        val bundle = Bundle()
        bundle.putSerializable(Constants.FILTER_RESTAURANTLISTMODEL,filterable_restaurantlistmodel)
        intent.putExtra(Constants.BUNDLE,bundle)
        setResult(Activity.RESULT_OK,intent)
        finish()




    }



    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")
    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }

    class MyClickHandler(val tilpas: Tilpas) {


        fun continue_btn (view: View) {
            Log.e("TAG","btn--")
            tilpas.filterbytilpas()
        }
        fun tilpas_row_item (view: View,model :RestaurantList.kokken_Model) {
            Log.e("TAG","tilpas--")
            if (model.is_itemselected) model.is_itemselected = false else model.is_itemselected=true
            tilpas.tilpassort_mAdapter.notifyDataSetChanged()
        }
        fun easy_row_item (view: View,model :RestaurantList.kokken_Model) {
            Log.e("TAG","easy--")
            for (i in 0 until tilpas.easysort_list.size){
                tilpas.easysort_list.get(i).is_itemselected=false
            }
            model.is_itemselected=true
            tilpas.easysort_mAdapter.notifyDataSetChanged()
        }

    }

    }