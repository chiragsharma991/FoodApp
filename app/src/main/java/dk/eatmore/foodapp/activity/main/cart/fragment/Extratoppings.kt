package dk.eatmore.foodapp.activity.main.cart.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CalculateAttribute
import dk.eatmore.foodapp.activity.main.cart.CalculateExtratoppings
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.adapter.extratoppings.ExtratoppingsAdapter
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.model.cart.Extra_topping_group_deatils
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_extratoppings.*

class Extratoppings : BaseFragment() {


    private lateinit var binding: FragmentExtratoppingsBinding
    private lateinit var mAdapter : ExtratoppingsAdapter


    companion object {

        val TAG = "Extratoppings"
        fun newInstance(parentPosition: Int, chilPosition: Int, viewmodel: CartActivity.UIModel, calculateExtratoppings : ArrayList<CalculateExtratoppings>): Extratoppings {
            val fragment = Extratoppings()
            val bundle =Bundle()
            bundle.putSerializable("extra_topping_group_deatils",viewmodel.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils)
            bundle.putSerializable("calculateExtratoppings", calculateExtratoppings)
            fragment.arguments=bundle
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_extratoppings
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            val extra_topping_group_deatils= arguments?.getSerializable("extra_topping_group_deatils") as Extra_topping_group_deatils
            val calculateExtratoppings= arguments?.getSerializable("calculateExtratoppings") as ArrayList<CalculateExtratoppings>
            recycler_view_extratoppings.apply {
                mAdapter = ExtratoppingsAdapter(context!!, extra_topping_group_deatils.topping_subgroup_list,calculateExtratoppings, object : ExtratoppingsAdapter.AdapterListener {
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

