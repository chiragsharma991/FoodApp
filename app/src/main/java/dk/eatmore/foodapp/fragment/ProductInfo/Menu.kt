package dk.eatmore.foodapp.fragment.ProductInfo

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_order_container.*

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
        recycler_view.apply {

            mAdapter = OrderListAdapter(context!!,object: OrderListAdapter.AdapterListener {
                override fun itemClicked(position: Int) {

                    loge(TAG,"on click....")
                }
            })

           // mAdapter = OrderListAdapter(context!!)
            layoutManager = LinearLayoutManager(getActivityBase())
            adapter = mAdapter
        }

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



