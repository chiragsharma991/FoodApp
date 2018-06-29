package dk.eatmore.foodapp.fragment

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FindrestaurantBinding
import dk.eatmore.foodapp.utils.BaseFragment
import android.widget.Toast
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.rest.ApiCall
import kotlinx.android.synthetic.main.findrestaurant.*


class FindRestaurant : BaseFragment() {

    private lateinit var binding: FindrestaurantBinding
    lateinit var clickEvent : MyClickHandler
    private  var mAdapter: OrderListAdapter?=null
    private lateinit var findRestaurantViewModel: FindRestaurantUIModel


    companion object {

        val TAG= "FindRestaurant"
        fun newInstance() : FindRestaurant {
            return FindRestaurant()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.findrestaurant
    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        clickEvent =MyClickHandler(this)
        binding.handlers=clickEvent
        mAdapter = OrderListAdapter(context!!)
        recycler_view.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view.adapter = mAdapter
        findRestaurantViewModel = ViewModelProviders.of(this).get(FindRestaurantUIModel::class.java!!)
         // log(TAG, "savedInstanceState..."+savedInstanceState)




        if(savedInstanceState ==null){

            fetchOrders(false)
            findRestaurantViewModel.getUser().observe(this, Observer<LastOrder> {

            })

            }
        else{

          //  log(TAG, "else View model..."+findRestaurantViewModel.uiData.value!!.msg)
      //      fetchOrders(false)

        }

      //  findRestaurantViewModel.getUser().observe(this, Observer { user -> Log.e("TAG", user!!.email)


    }



    fun fetchOrders(setAdapter : Boolean) {

        callAPI(ApiCall.myOrder(r_key = "fcARlrbZFXYee1W6eYEIA0VRlw7MgV4o07042017114812",r_token = "w5oRqFiAXTBB3hwpixAORbg_BwUj0EMQ07042017114812"),
                object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {

                loge(TAG, "api call success..."+(body as LastOrder).status)

                if ((body as LastOrder).status) {

                    findRestaurantViewModel.set(body)

                    loge(TAG, "View model..."+findRestaurantViewModel.uiData.value!!.msg)


                } else {

                }

            }

            override fun onFail(error: Int) {
                loge(TAG, "api call failed...")

                when (error) {
                    404 -> {

                    }
                    100 -> {


                    }

                }




            }

        })
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


    inner class  MyClickHandler(internal var findrestaurant: FindRestaurant) {



         fun onFindClicked(view: View) {
             Toast.makeText(findrestaurant.activity, "Button long pressed!", Toast.LENGTH_SHORT).show();
             Log.e("click","---")
             val last: LastOrder= LastOrder()
             last.msg="chirag"
             last.status=false
             findRestaurantViewModel.set(last)

         }

    }







}

class FindRestaurantUIModel : ViewModel() {

    var uiData = MutableLiveData<LastOrder>()

    fun init() {
        /* expensive operation, e.g. network request */
        val user= User()
        user.name ="vaibhav"
        user.email ="holle holle"

        //uiData.value = user
    }
    fun set(body: Any?) {
        /* expensive operation, e.g. network request */


        uiData.value = (body as LastOrder)
    }

    fun getUser(): LiveData<LastOrder> {
        return uiData
    }
}

