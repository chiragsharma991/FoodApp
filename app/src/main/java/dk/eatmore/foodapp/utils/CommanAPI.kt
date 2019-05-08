package dk.eatmore.foodapp.utils

import android.os.Build
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.transition.Slide
import android.view.Gravity
import android.view.View
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.Dashboard.Order.Data
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import retrofit2.Call

abstract class CommanAPI : BaseFragment() {


    abstract fun comman_apisuccess(jsonObject: JsonObject, api_tag: String)
    abstract fun comman_apifailed(error: String, api_tag: String)


    fun fetchReorder_info(model: OrderFragment.Orderresult, containerview: View) {

        showProgressDialog()
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.ORDER_NO, model.order_no)
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)

        callAPI(ApiCall.reorder(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                showProgressDialog()
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    // if restaurant is closed then block all next process.

                    when(getrestaurantstatus(is_restaurant_closed =jsonObject.get(Constants.IS_RESTAURANT_CLOSED)?.asBoolean, pre_order =jsonObject.get(Constants.PRE_ORDER)?.asBoolean )){

                        RestaurantState.CLOSED ->{
                            val msg = if (jsonObject.has(Constants.MSG)) jsonObject.get(Constants.MSG).asString else getString(R.string.sorry_restaurant_has_been_closed)
                            any_preorder_closedRestaurant(is_restaurant_closed = true ,pre_order = false,msg =msg ) // set hard code to close restaurant.
                        }
                        else ->{
                            // if restaurant wants to show some info only about then.
                            if (jsonObject.get(Constants.SHOW_MSG).asBoolean) {
                                val msg = if (jsonObject.has(Constants.MSG)) jsonObject.get(Constants.MSG).asString else getString(R.string.sorry_restaurant_has_been_closed)
                                DialogUtils.openDialogDefault(context = context!!, btnNegative = "", btnPositive = getString(R.string.ok), color = ContextCompat.getColor(context!!, R.color.black), msg = msg, title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                                    override fun onPositiveButtonClick(position: Int) {
                                        //  OrderFragment.ui_model!!.restaurant_info.value = myorder_Model // move this response to another list to reorder perpose.
                                        PreferenceUtil.putValue(PreferenceUtil.R_KEY, model.r_key)
                                        PreferenceUtil.putValue(PreferenceUtil.R_TOKEN, model.r_token)
                                        PreferenceUtil.save()
                                        comman_apisuccess(jsonObject, "")
                                    }

                                    override fun onNegativeButtonClick() {}
                                })
                            } else {
                                PreferenceUtil.putValue(PreferenceUtil.R_KEY, model.r_key)
                                PreferenceUtil.putValue(PreferenceUtil.R_TOKEN, model.r_token)
                                PreferenceUtil.save()
                                comman_apisuccess(jsonObject, "")

                                //   OrderFragment.ui_model!!.restaurant_info.value = myorder_Model // move this response to another list to reorder perpose.
                            }
                        }
                    }
                    /*     fetchRestaurant_info(
                                 model=model,
                                 msg = jsonObject.get(Constants.MSG).asString,
                                 cartamt =jsonObject.get(Constants.CARTAMT).asString,
                                 cartcnt = jsonObject.get(Constants.CARTCNT).asString,
                                 show_msg = jsonObject.get(Constants.SHOW_MSG).asBoolean,
                                 containerview = containerview
                         )*/

                } else {
                    showProgressDialog()
                    showSnackBar(containerview, getString(R.string.error_404))

                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(containerview, getString(R.string.error_404))
                        comman_apifailed(getString(R.string.error_404), "")
                    }
                    100 -> {
                        showSnackBar(containerview, getString(R.string.internet_not_available))
                        comman_apifailed(getString(R.string.internet_not_available), "")
                    }
                }
                showProgressDialog()
            }
        })
    }

    protected fun <T> setfavorite(call: Call<JsonObject>, restaurant: T?) {


        callAPI(call, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    if (restaurant != null) {
                        if (restaurant is Restaurant) {
                            restaurant.is_fav = true
                            restaurant.fav_progress = false
                        } else if (restaurant is Data) {
                            restaurant.is_fav = true
                        }
                    }
                    comman_apisuccess(jsonObject, Constants.COM_ADD_FAVORITE_RESTAURANT)
                } else {
                    if (restaurant != null) {
                        if (restaurant is Restaurant) {
                            restaurant.is_fav = false
                            restaurant.fav_progress = false
                        } else if (restaurant is Data) {
                            restaurant.is_fav = false
                        }
                    }
                    comman_apisuccess(jsonObject, Constants.COM_ADD_FAVORITE_RESTAURANT)
                }

            }

            override fun onFail(error: Int) {
                if (restaurant != null) {
                    if (restaurant is Restaurant) {
                        restaurant.is_fav = false
                        restaurant.fav_progress = false
                    } else if (restaurant is Data) {
                        restaurant.is_fav = false
                    }
                }
                when (error) {
                    404 -> {
                        // showSnackBar(containerview, getString(R.string.error_404))
                        loge("Comman API", getString(R.string.error_404))
                        comman_apifailed(getString(R.string.error_404), Constants.COM_ADD_FAVORITE_RESTAURANT)


                    }
                    100 -> {
                        // showSnackBar(containerview, getString(R.string.internet_not_available))
                        loge("Comman API", getString(R.string.internet_not_available))
                        comman_apifailed(getString(R.string.internet_not_available), Constants.COM_ADD_FAVORITE_RESTAURANT)

                    }
                }
            }
        })

    }

    protected fun <T> remove_favorite_restaurant(call: Call<JsonObject>, restaurant: T?) {


        callAPI(call, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    if (restaurant != null) {
                        if (restaurant is Restaurant) {
                            loge("Comman API", "restaurant type")
                            restaurant.is_fav = false
                            restaurant.fav_progress = false
                        } else if (restaurant is Data) {
                            restaurant.is_fav = false
                        }
                    }

                    comman_apisuccess(jsonObject, Constants.COM_ADD_FAVORITE_RESTAURANT)
                } else {
                    if (restaurant != null) {
                        if (restaurant is Restaurant) {
                            loge("Comman API", "restaurant type")
                            restaurant.is_fav = true  // if fail something then do not do unfavourite.
                            restaurant.fav_progress = false
                        } else if (restaurant is Data) {
                            restaurant.is_fav = true
                        }
                    }
                    comman_apisuccess(jsonObject, Constants.COM_ADD_FAVORITE_RESTAURANT)
                }

            }

            override fun onFail(error: Int) {
                if (restaurant != null) {
                    if (restaurant is Restaurant) {
                        loge("Comman API", "restaurant type")
                        restaurant.is_fav = true
                        restaurant.fav_progress = false
                    } else if (restaurant is Data) {
                        restaurant.is_fav = true
                    }
                }


                when (error) {
                    404 -> {
                        // showSnackBar(containerview, getString(R.string.error_404))
                        loge("Comman API", getString(R.string.error_404))
                        comman_apifailed(getString(R.string.error_404), Constants.COM_ADD_FAVORITE_RESTAURANT)


                    }
                    100 -> {
                        // showSnackBar(containerview, getString(R.string.internet_not_available))
                        loge("Comman API", getString(R.string.internet_not_available))
                        comman_apifailed(getString(R.string.internet_not_available), Constants.COM_ADD_FAVORITE_RESTAURANT)

                    }
                }
            }
        })

    }
    protected fun  checkinfo_restaurant_closed() {

        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)

        callAPI(ApiCall.restaurant_closed(jsonObject = postParam), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                    comman_apisuccess(jsonObject, Constants.COM_INFO_RESTAURANT_CLOSED)
            }

            override fun onFail(error: Int) {

                when (error) {
                    404 -> {
                        comman_apifailed(getString(R.string.error_404), Constants.COM_INFO_RESTAURANT_CLOSED)


                    }
                    100 -> {
                        comman_apifailed(getString(R.string.internet_not_available), Constants.COM_INFO_RESTAURANT_CLOSED)

                    }
                }
            }
        })

    }


    protected fun moveon_reOrder(status: String) {

        val fragment = DetailsFragment.newInstance(
                restaurant = null,
                status = "",
                ordertype = ""
        )
        var enter: Slide? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enter = Slide()
            enter.setDuration(300)
            enter.slideEdge = Gravity.BOTTOM
            fragment.enterTransition = enter
        }
        // pop all fragment on homecontainer to open reorder framgment.
        if ((activity as HomeActivity).fragmentTab_is() == 1) {
            // order fragment
            //showProgressDialog()
            val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            (fragmentof as HomeContainerFragment).getHomeFragment().popAllFragment()
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(0,0) // if user is login and press only back then move->Home
            Handler().postDelayed({
                //showProgressDialog()
                //HomeFragment.is_from_reorder=true
                HomeFragment.isFrom = HomeFragment.IsFrom.ORDER
                ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().reorderfuntion()
            }, 800)



         //   fragmentof.getOrderFragment().addFragment(R.id.home_order_container, fragment, DetailsFragment.TAG, false)
        } else {
            // Home fragment + Account fragment
            val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            (fragmentof as HomeContainerFragment).getOrderFragment().popAllFragment()
            fragmentof.getHomeFragment().addFragment(R.id.home_fragment_container, fragment, DetailsFragment.TAG, false)
        }
       // showTabBar(false)

    }


}