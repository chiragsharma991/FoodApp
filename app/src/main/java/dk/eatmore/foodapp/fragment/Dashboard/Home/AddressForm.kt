package dk.eatmore.foodapp.fragment.Dashboard.Home

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.HomeActivity
import dk.eatmore.foodapp.adapter.CartViewAdapter
import dk.eatmore.foodapp.databinding.RagistrationFormBinding
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class AddressForm : BaseFragment() {

    var transition : Transition?=null
    private val userList = ArrayList<User>()
    private  var mAdapter: CartViewAdapter?=null
    private lateinit var binding: RagistrationFormBinding
    private lateinit var homeFragment: HomeFragment




    companion object {
        val TAG="AddressForm"
        fun newInstance() : AddressForm {
            return AddressForm()
        }
    }

    override fun getLayout(): Int {
        return R.layout.ragistration_form
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            homeFragment=(fragmentof as HomeContainerFragment).getHomeFragment()
            DrawableCompat.setTint(ContextCompat.getDrawable(activity!!, R.drawable.close)!!, ContextCompat.getColor(activity!!, R.color.theme_color));
            txt_toolbar.text = ""
            txt_toolbar_right.visibility = View.VISIBLE
            txt_toolbar_right.text = getString(R.string.ok)
            toolbar.setNavigationIcon(ContextCompat.getDrawable(activity!!, R.drawable.close))
            toolbar.setNavigationOnClickListener {
                (activity as HomeActivity).onBackPressed()

            }

        }else{

        }


    }



}
