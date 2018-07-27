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
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerClickInterface
import dk.eatmore.foodapp.adapter.UniversalAdapter.UniversalAdapter
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
            address_list_view.visibility=View.VISIBLE
            ragistered_address_view.visibility=View.GONE
            logd(TAG,"saveInstance NULL")
            val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            homeFragment=(fragmentof as HomeContainerFragment).getHomeFragment()
            txt_toolbar.text="Address"
            txt_toolbar_right.visibility= View.VISIBLE
            txt_toolbar_right.text=getString(R.string.add)
            txt_toolbar_right.setOnClickListener{
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
                homeFragment.addFragment(R.id.home_fragment_container,fragment, AddressForm.TAG,false)


            }
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
                address_list_view.visibility=View.GONE
                //ragistered_address_view.startAnimation(AnimationUtils.loadAnimation(context,R.anim.enter_from_right))
                ragistered_address_view.visibility=View.VISIBLE
                val animation1 = AnimationUtils.loadAnimation(context,R.anim.enter_from_right)
                animation1.duration = 500 // animation duration
                ragistered_address_view.startAnimation(animation1)//your_view for mine is imageView
            }

            address_edt.setOnClickListener{

                val fragment = AddressForm.newInstance()
                var enter : Slide?=null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    enter = Slide()
                    enter!!.setDuration(300)
                    enter!!.slideEdge = Gravity.RIGHT
                    val changeBoundsTransition : ChangeBounds = ChangeBounds()
                    changeBoundsTransition.duration = 300
                    fragment.sharedElementEnterTransition=changeBoundsTransition
                    fragment.enterTransition=enter
                }
                homeFragment.addFragment(R.id.home_fragment_container,fragment, AddressForm.TAG,false)

            }


        }else{
            logd(TAG,"saveInstance NOT NULL")

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
