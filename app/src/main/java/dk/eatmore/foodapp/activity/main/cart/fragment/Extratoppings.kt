package dk.eatmore.foodapp.activity.main.cart.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bambora.nativepayment.handlers.BNPaymentHandler
import com.bambora.nativepayment.managers.CreditCardManager
import com.bambora.nativepayment.models.creditcard.CreditCard
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.epay.fragment.EditCart
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerClickInterface
import dk.eatmore.foodapp.adapter.UniversalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.cart.Extra_topping_group_deatils
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_add_cart.*
import kotlinx.android.synthetic.main.fragment_extratoppings.*
import java.util.ArrayList

class Extratoppings : BaseFragment() {


    private lateinit var binding: FragmentExtratoppingsBinding


    companion object {

        val TAG = "Extratoppings"
        fun newInstance(parentPosition: Int, chilPosition: Int, viewmodel: CartActivity.UIModel): Extratoppings {
            val fragment = Extratoppings()
          //  val bundle =Bundle()
            //bundle.putSerializable("extra_topping_group_deatils",viewmodel.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils)
           // fragment.arguments=bundle
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
            //val extra_topping_group_deatils= arguments?.getSerializable("extra_topping_group_deatils") as Extra_topping_group_deatils

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

