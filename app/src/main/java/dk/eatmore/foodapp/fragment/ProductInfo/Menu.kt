package dk.eatmore.foodapp.fragment.ProductInfo

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_order_container.*
import kotlinx.android.synthetic.main.menu_restaurant.*
import android.support.design.widget.TabLayout
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import dk.eatmore.foodapp.adapter.GenericAdapter
import dk.eatmore.foodapp.adapter.ViewHolderFactory
import dk.eatmore.foodapp.model.MenuRestaurant
import dk.eatmore.foodapp.model.User
import kotlinx.android.synthetic.main.rating_restaurant.*


class Menu : BaseFragment() {

    private lateinit var binding: FragmentAccountContainerBinding
    private  var mAdapter: OrderListAdapter?=null



    companion object {

        val TAG = "Menu"
        fun newInstance(): Menu {
            return Menu()
        }

    }


    override fun getLayout(): Int {
        return R.layout.menu_restaurant
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)

        //   binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
       // return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")


        }else{
            logd(TAG,"saveInstance NOT NULL")

        }

        tabs.addTab(tabs.newTab().setText("Delivery"))
        tabs.addTab(tabs.newTab().setText("PickUp"))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
                logd(TAG, tabs.selectedTabPosition.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

        })

        val list= listOf<Any>(MenuRestaurant("Pizza"), MenuRestaurant("Coca-Cola"),MenuRestaurant("Deep-Pan"), MenuRestaurant("venila"),MenuRestaurant("Brown stick"), MenuRestaurant("Choco Moko") )

        val mAdapter=object : GenericAdapter<Any>(list){
            override fun getLayoutId(position: Int, obj: Any): Int {
                return when(obj){
                    is MenuRestaurant -> R.layout.row_menu_restaurant
                    else -> R.layout.row_rating_list
                }
            }
            override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                return ViewHolderFactory.create(view,viewType)
            }
        }
        recycler_view_menu.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view_menu.adapter = mAdapter


    }


    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }

}



