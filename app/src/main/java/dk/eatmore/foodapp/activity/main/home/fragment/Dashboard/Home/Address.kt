package dk.eatmore.foodapp.fragment.Dashboard.Home

import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.transition.ChangeBounds
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.epay.fragment.DeliveryTimeslot
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerClickInterface
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentAddressBinding
import dk.eatmore.foodapp.databinding.RowAddressBinding
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_address.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.ArrayList


class Address : BaseFragment(), RecyclerClickInterface {


    private lateinit var binding: FragmentAddressBinding
    private val list = ArrayList<User>()
    private var mAdapter: UniversalAdapter<User, RowAddressBinding>? = null
    private lateinit var homeFragment: HomeFragment




    companion object {

        val TAG = "Address"
        fun newInstance(): Address {
            return Address()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_address
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            address_list_view.visibility=View.GONE
            ragistered_address_view.visibility=View.VISIBLE
            logd(TAG,"saveInstance NULL")
            setToolbarforThis()
            fillData()
            mAdapter = UniversalAdapter(context!!, list, R.layout.row_address, object : RecyclerCallback<RowAddressBinding, User> {
                override fun bindData(binder: RowAddressBinding, model: User) {
                    binder.user=model
                    binder.handler=this@Address
                }
            })
            recycler_view_address.layoutManager = LinearLayoutManager(getActivityBase())
            recycler_view_address.adapter = mAdapter

            proceed_view.setOnClickListener{
                (activity as EpayActivity).txt_toolbar_right.visibility= View.GONE
                address_list_view.visibility=View.GONE
                ragistered_address_view.visibility=View.VISIBLE
            }

            address_edt.setOnClickListener{
                (activity as EpayActivity).txt_toolbar_right.visibility= View.VISIBLE
                ragistered_address_view.visibility=View.GONE
                address_list_view.visibility=View.VISIBLE
            }

            proceed_view_nxt.setOnClickListener{
                val fragment = DeliveryTimeslot.newInstance()
                (activity as EpayActivity).addFragment(R.id.epay_container,fragment, DeliveryTimeslot.TAG,true)
            }

        }else{
            logd(TAG,"saveInstance NOT NULL")
            (activity as EpayActivity).popWithTag(Address.TAG)
        }

    }


    fun setToolbarforThis(){

        (activity as EpayActivity).txt_toolbar.text=getString(R.string.address)
        (activity as EpayActivity).img_toolbar_back.setImageResource(R.drawable.back)
        (activity as EpayActivity).img_toolbar_back.setOnClickListener {
            loge(TAG,"address---")
            if(address_list_view.visibility==View.VISIBLE){
                address_list_view.visibility=View.GONE
                ragistered_address_view.visibility=View.VISIBLE
                (activity as EpayActivity).txt_toolbar_right.visibility=View.GONE
            }else{
                (activity as EpayActivity).popFragment()
                (activity as EpayActivity).setToolbarforThis()
            }
        }
        (activity as EpayActivity).txt_toolbar_right.visibility= if(address_list_view.visibility==View.VISIBLE) View.VISIBLE else View.GONE
        (activity as EpayActivity).txt_toolbar_right.text=getString(R.string.add)
        (activity as EpayActivity).txt_toolbar_right.setOnClickListener{
            val fragment = AddressForm.newInstance()
            var enter : Slide?=null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                enter = Slide()
                enter.setDuration(300)
                enter.slideEdge = Gravity.BOTTOM
                val changeBoundsTransition : ChangeBounds = ChangeBounds()
                changeBoundsTransition.duration = 300
                fragment.sharedElementEnterTransition=changeBoundsTransition
                fragment.enterTransition=enter
            }
            addFragment(R.id.address_container,fragment, AddressForm.TAG,false)
        }

    }


        override fun onClick(user: User) {


    }

    private fun fillData() {
        val user1 = User()
        user1.name="Standard"
        list.add(user1)

        val user2 = User()
        user2.name="4000"
        list.add(user2)

        val user3 = User()
        user3.name="PatwarPhalla"
        list.add(user3)

        val user4 = User()
        user4.name="112,VFX"
        list.add(user4)


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
