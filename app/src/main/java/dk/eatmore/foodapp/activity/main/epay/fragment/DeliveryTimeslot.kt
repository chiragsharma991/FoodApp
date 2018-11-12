package dk.eatmore.foodapp.activity.main.epay.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.databinding.DeliverytimeslotBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.deliverytimeslot.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlin.collections.ArrayList

class DeliveryTimeslot : BaseFragment() {

    var transition: Transition? = null
    private val userList = ArrayList<User>()
    private var mAdapter: CartViewAdapter? = null
    private lateinit var binding: DeliverytimeslotBinding
    private lateinit var homeFragment: HomeFragment
    private var timeslot: ArrayList<String>?=null
    private var selectedtimeslot_position : Int = 0
    private  var time_list: LinkedHashMap<String,String>?=null


    companion object {
        val TAG = "DeliveryTimeslot"
        fun newInstance(time_list: LinkedHashMap<String, String>?): DeliveryTimeslot {
            val bundle = Bundle()
            val fragment = DeliveryTimeslot()
            bundle.putSerializable(Constants.TIME_LIST,time_list)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayout(): Int {
        return R.layout.deliverytimeslot
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            time_list= arguments?.getSerializable(Constants.TIME_LIST) as? LinkedHashMap<String, String>
            setToolbarforThis()
            if(time_list !=null){
                binding.isLoading=false
                for (entries in time_list!!.entries ){
                    // loop for one time to get first index name:
                    delivery_time_slot.text= time_list!![entries.key].toString()
                    break
                }
            }
            else{
                fetch_PickupTime()
                binding.isLoading=true
            }
            delivery_time_slot.setOnClickListener {
                if( time_list?.size ?: 0 > 0 ){

                    val list = ArrayList<String>()
                    for (entries in time_list!!.entries ){
                        list.add(time_list!![entries.key].toString())
                    }
                    selectdeliverytime(list)
                }
                else {
                    seterror(address_container)
                }
            }
            secure_payment_btn.setOnClickListener{
                if(time_list?.size ?:0 > 0){
                    for ( entries in time_list!!.entries){
                        if(time_list!!.get(entries.key) == delivery_time_slot.text.trim().toString()){
                            EpayActivity.paymentattributes.expected_time=entries.key
                        }
                    }
                    EpayActivity.paymentattributes.comments=comment_edt.text.trim().toString()
                    (activity as EpayActivity).addFragment(R.id.epay_container,Paymentmethod.newInstance(),Paymentmethod.TAG,true)
                }
                else {
                    seterror(address_container)
                }
            }

        }else{
            (activity as EpayActivity).popWithTag(DeliveryTimeslot.TAG)
        }
    }



    private fun fetch_PickupTime() {

        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.SHIPPING, if (EpayActivity.isPickup) getString(R.string.pickup) else getString(R.string.delivery))

        callAPI(ApiCall.pickupinfo(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {

                    time_list =LinkedHashMap()
                    for (i in 0.until(jsonobject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).size())){
                        time_list!!.put((jsonobject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).get(i) as JsonObject)[Constants.ACTUAL].asString,
                                (jsonobject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).get(i) as JsonObject)[Constants.DISPLAY].asString )
                    }
                    for (entries in time_list!!.entries ){
                        // loop for one time to get first index name:
                        delivery_time_slot.text= time_list!![entries.key].toString()
                        break
                    }
                    EpayActivity.paymentattributes.additional_charges_cash=if(jsonobject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_CASH).asString=="") "0" else jsonobject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_CASH).asString
                    EpayActivity.paymentattributes.additional_charges_online=if(jsonobject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_ONLINE].asString=="") "0" else jsonobject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_ONLINE].asString
                    EpayActivity.paymentattributes.first_time=jsonobject.getAsJsonObject(Constants.RESULT).get(Constants.FIRST_TIME).asString
                    binding.isLoading=false

                }else{
                    showSnackBar(address_container, getString(R.string.error_404))
                    binding.isLoading=false

                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(address_container, getString(R.string.error_404))
                        binding.isLoading=false
                    }
                    100 -> {
                        showSnackBar(address_container, getString(R.string.internet_not_available))
                        binding.isLoading=false

                    }
                }
                //showProgressDialog()


            }
        })


    }


    private fun selectdeliverytime(timeslot: ArrayList<String>) {

        val item = arrayOfNulls<String>(timeslot.size)
        timeslot.toArray(item)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.select_delivery_time))
        builder.setSingleChoiceItems(item, selectedtimeslot_position, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, item_position: Int) {
                selectedtimeslot_position=item_position
                delivery_time_slot.text=item[item_position]
            }
        })
                .setPositiveButton("Done!", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {

                    }
                })
                .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {}
                })
        val dialog = builder.create()
        dialog.show()
    }


    // set common toolbar from this and set pre fragment toolbar from this.

    fun setToolbarforThis() {
        (activity as EpayActivity).txt_toolbar.text = getString(R.string.confirm_delivery_time)
      //  (activity as EpayActivity).img_toolbar_back.setOnClickListener { onBackpress() }
    }

    fun onBackpress() {
        (activity as EpayActivity).popFragment()

    }


}