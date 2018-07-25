package dk.eatmore.foodapp.activity.main.epay.fragment


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_add_cart.*

class AddCart : BaseFragment() {

    private lateinit var binding: FragmentAddCartBinding
    private lateinit var editCart_fragment: EditCart



    companion object {

        val TAG = "AddCart"
        fun newInstance(): AddCart {
            return AddCart()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_add_cart
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            add_new_card.setOnClickListener{
                editCart_fragment = EditCart.newInstance()
                (activity as EpayActivity).addFragment(R.id.epay_container, editCart_fragment, EditCart.TAG, true)
            }


        }else{
            logd(TAG,"saveInstance NOT NULL")

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

