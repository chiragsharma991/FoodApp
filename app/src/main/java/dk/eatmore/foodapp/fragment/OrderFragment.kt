package dk.eatmore.foodapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.utils.BaseFragment

class OrderFragment : BaseFragment() {



    companion object {

        val TAG= "OrderFragment"
        fun newInstance() : OrderFragment {
            return OrderFragment()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)

    }

    override fun getLayout(): Int {
        return R.layout.fragment_order_container
    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {



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
}



