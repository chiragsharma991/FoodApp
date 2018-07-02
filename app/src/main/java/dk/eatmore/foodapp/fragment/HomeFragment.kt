package dk.eatmore.foodapp.fragment


import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.databinding.FindrestaurantBinding
import dk.eatmore.foodapp.databinding.FragmentHomeFragmentBinding
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.findrestaurant.*


class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeFragmentBinding
    lateinit var clickEvent : HomeFragment.MyClickHandler
    private  var mAdapter: OrderListAdapter?=null


    companion object {

        val TAG= "HomeFragment"
        fun newInstance() : HomeFragment {
            return HomeFragment()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       // return inflater.inflate(getLayout(), container, false)

        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }

    override fun getLayout(): Int {
        return R.layout.fragment_home_fragment
    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {

        clickEvent =MyClickHandler(this)
        binding.handlers=clickEvent
        mAdapter = OrderListAdapter(context!!)
        recycler_view.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view.adapter = mAdapter


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

    inner class  MyClickHandler(internal var homefragment: HomeFragment) {


        fun onFindClicked(view: View) {
            Toast.makeText(homefragment.activity, "Button long pressed!", Toast.LENGTH_SHORT).show();
            Log.e("click","---")

        }

    }





}