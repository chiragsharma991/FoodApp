package dk.eatmore.foodapp.fragment.Dashboard


import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
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
import dk.eatmore.foodapp.databinding.FragmentHomeFragmentBinding
import dk.eatmore.foodapp.model.HomeFragment.UI_HomeFragment
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import kotlinx.android.synthetic.main.fragment_home_fragment.*


class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeFragmentBinding
    lateinit var clickEvent : MyClickHandler
    private  var mAdapter: OrderListAdapter?=null
    private var fragment: DetailsFragment?=null


    companion object {

        val TAG= "HomeFragment"
        fun newInstance() : HomeFragment {
            return HomeFragment()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
     //   return inflater.inflate(getLayout(), container, false)

        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }

    override fun getLayout(): Int {
        return R.layout.fragment_home_fragment
    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {

        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            clickEvent =MyClickHandler(this)
            binding.handlers=clickEvent

        }else{
            logd(TAG,"saveInstance NOT NULL")

        }

        recycler_view.apply {
            mAdapter = OrderListAdapter(context!!,object: OrderListAdapter.AdapterListener {
                override fun itemClicked(position: Int) {
                    loge(TAG,"on click....")
                    fragment = DetailsFragment.newInstance()
                    addFragment(R.id.home_fragment_container,fragment!!,DetailsFragment.TAG)

                }
            })
            layoutManager = LinearLayoutManager(getActivityBase())
            adapter = mAdapter
        }




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

    inner private class UIModel : ViewModel(){

        var uiData = MutableLiveData<UI_HomeFragment>()

        fun init() {
            val ui_homefragment= UI_HomeFragment("HomeFragment",false)
            uiData.value = ui_homefragment
        }
        fun set(body: Any?) {
            /* expensive operation, e.g. network request */
            //uiData.value = (body as LastOrder)
        }

        fun getUIModel(): LiveData<UI_HomeFragment> {
            return uiData
        }


    }





}

