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
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.rest.ApiCall
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


    companion object {
        val TAG = "DeliveryTimeslot"
        fun newInstance(): DeliveryTimeslot {
            return DeliveryTimeslot()
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
            binding.isLoading=true
            setToolbarforThis()
            fetch_PickupTime()
            delivery_time_slot.setOnClickListener {
                if(timeslot?.size ?:0 > 0){
                    selectdeliverytime(timeslot!!)
                }
                else {
                    seterror(address_container)
                }
            }
            secure_payment_btn.setOnClickListener{
                if(timeslot?.size ?:0 > 0){
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

        callAPI(ApiCall.getPickuptime(
                r_token = Constants.R_TOKEN,
                r_key = Constants.R_KEY,
                shipping = "Pickup",
                language = "en"
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonobject = body as JsonObject
                if (jsonobject.get("status").asBoolean) {
                    timeslot = ArrayList()
                    for (i in 0 until jsonobject.getAsJsonArray("times").size()) {
                        timeslot!!.add(jsonobject.getAsJsonArray("times")[i].asJsonObject.get("dt").asString)
                    }
                    delivery_time_slot.text=timeslot!![0]
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
        (activity as EpayActivity).txt_toolbar.text = ""
        (activity as EpayActivity).img_toolbar_back.setOnClickListener { onBackpress() }
    }

    fun onBackpress() {
        val frag = (activity as EpayActivity).supportFragmentManager.findFragmentByTag(Address.TAG)
        (frag as Address).setToolbarforThis()
        (activity as EpayActivity).popFragment()

    }


}