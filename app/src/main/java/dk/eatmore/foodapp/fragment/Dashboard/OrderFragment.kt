package dk.eatmore.foodapp.fragment.Dashboard

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.databinding.FragmentOrderContainerBinding
import dk.eatmore.foodapp.model.OrderFragment.UI_OrderFragment
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_order_container.*
import kotlinx.android.synthetic.main.toolbar.*

class OrderFragment : BaseFragment() {

    private lateinit var binding: FragmentOrderContainerBinding
    private  var mAdapter: OrderListAdapter?=null
    private  var ui_model: UIModel?=null
    val refFragment: OrderFragment = this



    companion object {

        val TAG= "OrderFragment"
        fun newInstance() : OrderFragment {
            return OrderFragment()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root


    }

    override fun getLayout(): Int {
        return R.layout.fragment_order_container
    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {

        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            txt_toolbar.text=getString(R.string.orders)
            recycler_view.apply {

                mAdapter = OrderListAdapter(context!!,object: OrderListAdapter.AdapterListener {
                    override fun itemClicked(position: Int) {
                        loge(TAG,"on click....")
                    }
                })
                layoutManager = LinearLayoutManager(getActivityBase())
                adapter = mAdapter
            }



        }else{
            logd(TAG,"saveInstance NOT NULL")
        }

        /* ui_model = ViewModelProviders.of(this).get(UIModel::class.java)
        ui_model!!.getUIModel().observe(this, Observer<UI_OrderFragment>{
            loge(TAG,"observer success---")
            binding.uiOrder=ui_model!!.getUIModel().value
        })
        ui_model!!.init()*/



    }



    override fun onDestroy() {
        super.onDestroy()
        logd(TAG,"on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG,"on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG,"on pause...")

    }

}


private class UIModel : ViewModel(){

    var uiData = MutableLiveData<UI_OrderFragment>()

    fun init() {
        val ui_orderfragment= UI_OrderFragment("Order",false)
        uiData.value = ui_orderfragment
    }
    fun set(body: Any?) {
        /* expensive operation, e.g. network request */
        //uiData.value = (body as LastOrder)
    }

    fun getUIModel(): LiveData<UI_OrderFragment> {
        return uiData
    }


}



