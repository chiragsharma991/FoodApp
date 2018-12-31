package dk.eatmore.foodapp.activity.main.filter



import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.utils.BaseActivity
import android.view.View
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.EditAddress
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.SelectAddress
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.ActivityKokkentypeBinding
import dk.eatmore.foodapp.databinding.RowKokkentypeBinding
import dk.eatmore.foodapp.databinding.RowSelectAddressBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.select_address.*
import kotlinx.android.synthetic.main.toolbar.*
import java.sql.Struct
import java.util.*
import kotlin.collections.ArrayList

class KokkenType : Kokken_tilpas_filter() {

    private lateinit var binding: ActivityKokkentypeBinding

    private lateinit var kokkenType_list: ArrayList<RestaurantList.kokken_Model>
    private var cpy_kokkenType_list: ArrayList<RestaurantList.kokken_Model> = ArrayList()
    private lateinit var easysort_list: ArrayList<RestaurantList.kokken_Model>
    private lateinit var tilpassort_list: ArrayList<RestaurantList.kokken_Model>
    private lateinit var restaurantlistmodel: RestaurantListModel

    private lateinit var filterable_restaurantlistmodel: RestaurantListModel

    private var myclickhandler: MyClickHandler = MyClickHandler(this)
    private lateinit var mAdapter: UniversalAdapter<RestaurantList.kokken_Model, RowKokkentypeBinding>


    companion object {
        val TAG = "KokkenType"
        fun newInstance(): KokkenType {
            return KokkenType()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_kokkentype)
        initView(savedInstanceState)

    }



    private fun initView(savedInstanceState: Bundle?) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        binding.handler=myclickhandler
        img_toolbar_back.setImageResource(R.drawable.close)
        img_toolbar_back.setOnClickListener{onBackPressed()}
        txt_toolbar.setText(getString(R.string.kokkentype))
        kokkenType_list  = RestaurantList.ui_model!!.kokkenType_list
        easysort_list =RestaurantList.ui_model!!.easysort_list
        tilpassort_list =RestaurantList.ui_model!!.tilpassort_list
        restaurantlistmodel  = intent.getBundleExtra(Constants.BUNDLE).getSerializable(Constants.KOKKEN_RESTAURANTLISTMODEL) as RestaurantListModel
        for (i in 0 until kokkenType_list.size){
            cpy_kokkenType_list.add(RestaurantList.kokken_Model(itemtype = kokkenType_list[i].itemtype,itemcount =kokkenType_list[i].itemcount,is_itemselected = kokkenType_list[i].is_itemselected))
        }
        refreshview()


    }



    private fun refreshview(){
        loge(SelectAddress.TAG,"refresh view...")
        mAdapter = UniversalAdapter(this,kokkenType_list, R.layout.row_kokkentype, object : RecyclerCallback<RowKokkentypeBinding,RestaurantList.kokken_Model> {
            override fun bindData(binder: RowKokkentypeBinding, model: RestaurantList.kokken_Model) {
                binder.kokkenModel=model
                binder.myClickHandler=myclickhandler
                binder.executePendingBindings()

            }
        })
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = mAdapter
    }

    private fun  applyFilter(){

        filterbyKokken(easysort_list = easysort_list,
                kokkenType_list = kokkenType_list,
                tilpassort_list =tilpassort_list,
                restaurantlistmodel = restaurantlistmodel )
    }

    class MyClickHandler(val kokkentype_: KokkenType) {


        fun continue_btn (view: View) {
            kokkentype_.applyFilter()
        }

        fun item_check (view : View, model :RestaurantList.kokken_Model) {
            var any_item_selected : Boolean = false
            if(model.itemtype.trim() == kokkentype_.getString(R.string.all).trim()){
                // check ALL
                for(i in 0 until kokkentype_.kokkenType_list.size){
                    if(kokkentype_.kokkenType_list.get(i).itemtype.trim() == kokkentype_.getString(R.string.all).trim())
                    if(kokkentype_.kokkenType_list.get(i).is_itemselected) kokkentype_.kokkenType_list.get(i).is_itemselected=false else kokkentype_.kokkenType_list.get(i).is_itemselected=true
                    else
                    kokkentype_.kokkenType_list.get(i).is_itemselected=false
                }

            }else{
                // check  without ALL
                if(model.is_itemselected) model.is_itemselected=false else model.is_itemselected=true
                for(i in 0 until kokkentype_.kokkenType_list.size){
                    if(kokkentype_.kokkenType_list.get(i).itemtype.trim() == kokkentype_.getString(R.string.all).trim())
                    kokkentype_.kokkenType_list.get(i).is_itemselected=false
                }
            }

            for(i in 0 until kokkentype_.kokkenType_list.size){
                // test if all list have been unchecked.
             if(kokkentype_.kokkenType_list.get(i).is_itemselected){
                 any_item_selected=true
             }
            }
            if(!any_item_selected){
                // select only All
                for(i in 0 until kokkentype_.kokkenType_list.size){
                    if(kokkentype_.kokkenType_list.get(i).itemtype.trim() == kokkentype_.getString(R.string.all).trim())
                        kokkentype_.kokkenType_list.get(i).is_itemselected=true
                }
            }
            kokkentype_.mAdapter.notifyDataSetChanged()
        }
    }


    override fun onBackPressed() {

        // if user did not changed , then retain all state.
        for (i in 0 until cpy_kokkenType_list.size){
            kokkenType_list.get(i).is_itemselected= if(cpy_kokkenType_list.get(i).is_itemselected) true else false
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


}