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


class Tilpas : Kokken_tilpas_filter() {


    private lateinit var binding: ActivityTilpasBinding

    private lateinit var easysort_list: ArrayList<RestaurantList.kokken_Model>
    private var cpy_easysort_list: ArrayList<RestaurantList.kokken_Model> = ArrayList()
    private lateinit var tilpassort_list: ArrayList<RestaurantList.kokken_Model>
    private var cpy_tilpassort_list: ArrayList<RestaurantList.kokken_Model> = ArrayList()
    private lateinit var kokkenType_list: ArrayList<RestaurantList.kokken_Model>
    private lateinit var restaurantlistmodel: RestaurantListModel // total selected restaurant from restaurant list (which is still filtered from kokken type)

    private lateinit var filterable_restaurantlistmodel: RestaurantListModel


    private lateinit var tilpassort_mAdapter: UniversalAdapter<RestaurantList.kokken_Model, RowTilpasSortBinding>
    private lateinit var easysort_mAdapter: UniversalAdapter<RestaurantList.kokken_Model, RowEasySortBinding>



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
        img_toolbar_back.setOnClickListener{onBackPressed()}
        txt_toolbar.setText(getString(R.string.tilpas))
        binding.handler=myclickhandler
        restaurantlistmodel  = intent.getBundleExtra(Constants.BUNDLE).getSerializable(Constants.TILPAS_RESTAURANTLISTMODEL) as RestaurantListModel
        easysort_list =RestaurantList.ui_model!!.easysort_list
        tilpassort_list =RestaurantList.ui_model!!.tilpassort_list
        kokkenType_list  = RestaurantList.ui_model!!.kokkenType_list
        for (i in 0 until easysort_list.size){
            cpy_easysort_list.add(RestaurantList.kokken_Model(itemtype = easysort_list[i].itemtype,itemcount =easysort_list[i].itemcount,is_itemselected = easysort_list[i].is_itemselected))
        }
        for (i in 0 until tilpassort_list.size){
            cpy_tilpassort_list.add(RestaurantList.kokken_Model(itemtype = tilpassort_list[i].itemtype,itemcount =tilpassort_list[i].itemcount,is_itemselected = tilpassort_list[i].is_itemselected))
        }
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

    private fun applyFilter(){


        filterbyKokken(easysort_list = easysort_list,
                kokkenType_list = kokkenType_list,
                tilpassort_list =tilpassort_list,
                restaurantlistmodel = restaurantlistmodel )

    }


    override fun onBackPressed() {
        // if user did not changed , then retain all state.
        for (i in 0 until cpy_easysort_list.size){
            easysort_list.get(i).is_itemselected= if(cpy_easysort_list.get(i).is_itemselected) true else false
        }
        for (i in 0 until cpy_tilpassort_list.size){
            tilpassort_list.get(i).is_itemselected= if(cpy_tilpassort_list.get(i).is_itemselected) true else false
        }

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
            tilpas.applyFilter()
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