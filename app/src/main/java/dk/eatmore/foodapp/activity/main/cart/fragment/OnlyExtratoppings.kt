package dk.eatmore.foodapp.activity.main.cart.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CalculateExtratoppings
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.adapter.extratoppings.ExtratoppingsAdapter
import dk.eatmore.foodapp.adapter.onlyextratoppings.OnlyExtratoppingsAdapter
import dk.eatmore.foodapp.databinding.FragmentExtratoppingsBinding
import dk.eatmore.foodapp.databinding.FragmentOnlyExtratoppingsBinding
import dk.eatmore.foodapp.model.cart.Extra_topping_group_deatils
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_extratoppings.*
import kotlinx.android.synthetic.main.fragment_only_extratoppings.*

class OnlyExtratoppings : BaseFragment() {


    private lateinit var binding: FragmentOnlyExtratoppingsBinding
    private lateinit var mAdapter : OnlyExtratoppingsAdapter


    companion object {

        val TAG = "OnlyExtratoppings"
        fun newInstance(extra_topping_group_deatils:Extra_topping_group_deatils): OnlyExtratoppings {
            val fragment = OnlyExtratoppings()
            val bundle = Bundle()
            bundle.putSerializable("extra_topping_group_deatils",extra_topping_group_deatils)
            fragment.arguments=bundle
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_only_extratoppings
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            val extra_topping_group_deatils= arguments?.getSerializable("extra_topping_group_deatils") as Extra_topping_group_deatils
            recycler_view_onlyextratoppings.apply {
                mAdapter = OnlyExtratoppingsAdapter(context!!, extra_topping_group_deatils.topping_subgroup_list, object : OnlyExtratoppingsAdapter.AdapterListener {
                    override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {
                        /*     if (ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils != null) {
                                 val fragment = newInstance(parentPosition, chilPosition, ui_model!!)
                                 addFragment(R.id.cart_container, fragment, TAG, true)
                             }*/
                    }
                })
                layoutManager = LinearLayoutManager(context)
                adapter = mAdapter
            }

        } else {
            logd(TAG, "saveInstance NOT NULL")

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
