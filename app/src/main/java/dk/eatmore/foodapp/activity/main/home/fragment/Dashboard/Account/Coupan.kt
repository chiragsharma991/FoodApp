package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.net.ParseException
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.gift.GiftBalanceParentAdapter
import dk.eatmore.foodapp.databinding.FragmentCoupanBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_account_container.*
import kotlinx.android.synthetic.main.fragment_coupan.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.util.*

class Coupan : BaseFragment() {

    private lateinit var binding: FragmentCoupanBinding
    private lateinit var list : ArrayList<Profile.GiftType>
    private val myclickhandler : MyClickHandler = MyClickHandler(this)


    companion object {

        val TAG = "Coupan"
        fun newInstance(list : ArrayList<Profile.GiftType>): Coupan {
            val fragment = Coupan()
            val bundle = Bundle()
            bundle.putSerializable("GiftType",list)
            fragment.arguments = bundle
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_coupan
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")

            @Suppress("UNCHECKED_CAST")
            list = arguments!!.getSerializable("GiftType") as  ArrayList<Profile.GiftType>
            txt_toolbar.text=getString(R.string.giftcard)
            img_toolbar_back.setOnClickListener{(activity as HomeActivity).onBackPressed()}
            if(list.size > 0){
                // data is present
                refresh()
            }else{
                // no data found
                Toast.makeText(context,getString(R.string.no_data_found),Toast.LENGTH_SHORT).show()
            }


        }else{
            logd(TAG,"saveInstance NOT NULL")

        }


    }


    private fun refresh(){

        recycler_view_parent.apply {
            loge(TAG,"recycler_view_parent---")
           val mAdapter = GiftBalanceParentAdapter(myclickhandler,context!!, list, object : GiftBalanceParentAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {
                }
            })
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }


    }

    fun ordernow(giftcardsinfo: Profile.GiftcardsInfo){

        PreferenceUtil.putValue(PreferenceUtil.R_KEY, giftcardsinfo.r_key)
        PreferenceUtil.putValue(PreferenceUtil.R_TOKEN, giftcardsinfo.r_token)
        PreferenceUtil.save()

        val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
        (fragmentof as HomeContainerFragment).getHomeFragment().popAllFragment()
        ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(0,0) // if user is login and press only back then move->Home
        Handler().postDelayed({
            //showProgressDialog()
            //HomeFragment.is_from_reorder=true
            HomeFragment.isFrom=HomeFragment.IsFrom.PROFILE
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().reorderfuntion()
        }, 800)

    }



    class MyClickHandler(val coupan: Coupan) {

        fun on_click (view: View, giftcardsinfo: Profile.GiftcardsInfo) {

            coupan.ordernow(giftcardsinfo)

        }


        fun parseTimeTommddyy(time:String):String {
            val inputPattern = "yyyy-MM-dd"
            val outputPattern = "MM-dd-yyyy"
            //   val outputPattern = "dd-MMM-yyyy h:mm a"
            val inputFormat = SimpleDateFormat(inputPattern)
            val outputFormat = SimpleDateFormat(outputPattern)
            var date: Date? = null
            var str:String? = null
            try
            {
                date = inputFormat.parse(time)
                str = outputFormat.format(date)
            }
            catch (e: ParseException) {
                e.printStackTrace()
            }
            return str!!
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
