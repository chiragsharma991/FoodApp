package dk.eatmore.foodapp.utils

import android.os.Build
import android.support.v4.content.ContextCompat
import android.transition.Slide
import android.view.Gravity
import android.view.View
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import kotlinx.android.synthetic.main.fragment_order_container.*

abstract class CommanAPI : BaseFragment(){

    abstract fun comman_apisuccess(msg : String, model: OrderFragment.Myorder_Model)
    abstract fun comman_apifailed(error : String)


    fun fetchReorder_info(model : OrderFragment.Orderresult, containerview : View) {

        showProgressDialog()
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP,true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.ORDER_NO,model.order_no )

        callAPI(ApiCall.reorder(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val jsonObject= body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    fetchRestaurant_info(
                            model=model,
                            msg = jsonObject.get(Constants.MSG).asString,
                            cartamt =jsonObject.get(Constants.CARTAMT).asString,
                            cartcnt = jsonObject.get(Constants.CARTCNT).asString,
                            show_msg = jsonObject.get(Constants.SHOW_MSG).asBoolean,
                            containerview = containerview
                    )

                }else{
                    showProgressDialog()
                    showSnackBar(containerview, getString(R.string.error_404))

                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(containerview, getString(R.string.error_404))
                        comman_apifailed(getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(containerview, getString(R.string.internet_not_available))
                        comman_apifailed(getString(R.string.internet_not_available))
                    }
                }
                showProgressDialog()
            }
        })
    }



    fun fetchRestaurant_info(model : OrderFragment.Orderresult, cartcnt : String, cartamt : String, show_msg : Boolean, msg : String, containerview : View) {


        callAPI(ApiCall.restaurant_info(
                r_token = model.r_token,
                r_key = model.r_key,
                customer_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,"")!!

        ), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val myorder_Model= body as OrderFragment.Myorder_Model
                if (myorder_Model.status) {
                    PreferenceUtil.putValue(PreferenceUtil.R_KEY, model.r_key)
                    PreferenceUtil.putValue(PreferenceUtil.R_TOKEN,model.r_token)
                    myorder_Model.restaurant_info.cartamt=cartamt
                    myorder_Model.restaurant_info.cartcnt=cartcnt
                    PreferenceUtil.save()
                    // if restaurant is closed then block all next process.
                    if(model.is_restaurant_closed){
                        DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!,R.color.black),msg =getString(R.string.we_are_sorry),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                            override fun onPositiveButtonClick(position: Int) {}
                            override fun onNegativeButtonClick() {}
                        })
                    }else{
                        // if restaurant wants to show some info about then.
                        if(show_msg){
                            DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!,R.color.black),msg =msg,title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                                override fun onPositiveButtonClick(position: Int) {
                                    //  OrderFragment.ui_model!!.restaurant_info.value = myorder_Model // move this response to another list to reorder perpose.
                                    comman_apisuccess(myorder_Model.msg,myorder_Model)
                                }
                                override fun onNegativeButtonClick() {}
                            })
                        }else{
                            comman_apisuccess(myorder_Model.msg,myorder_Model)

                            //   OrderFragment.ui_model!!.restaurant_info.value = myorder_Model // move this response to another list to reorder perpose.
                        }

                    }

                }
                showProgressDialog()
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(containerview, getString(R.string.error_404))
                        comman_apifailed(getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(containerview, getString(R.string.internet_not_available))
                        comman_apifailed(getString(R.string.internet_not_available))

                    }
                }
                showProgressDialog()
            }
        })
    }


    protected fun moveon_reOrder(model : OrderFragment.Myorder_Model){

        val fragment = DetailsFragment.newInstance(
                restaurant =  model.restaurant_info,
                status =     ""
        )
        var enter : Slide?=null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enter = Slide()
            enter.setDuration(300)
            enter.slideEdge = Gravity.BOTTOM
            fragment.enterTransition=enter
        }
        // pop all fragment on homecontainer to open reorder framgment.
        if((activity as HomeActivity).fragmentTab_is() == 1){
            // order fragment
            val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            (fragmentof as HomeContainerFragment).getHomeFragment().popAllFragment()
            fragmentof.getOrderFragment().addFragment(R.id.home_order_container,fragment, DetailsFragment.TAG,false)
        }else{
            // Home fragment + Account fragment
            val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            (fragmentof as HomeContainerFragment).getOrderFragment().popAllFragment()
            fragmentof.getHomeFragment().addFragment(R.id.home_fragment_container,fragment, DetailsFragment.TAG,false)
        }
        showTabBar(false)

    }


}