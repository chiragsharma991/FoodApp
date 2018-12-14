package dk.eatmore.foodapp.activity.main

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.databinding.ActivityEpayBinding
import dk.eatmore.foodapp.databinding.RestaurantclosedBinding
import dk.eatmore.foodapp.utils.BaseActivity
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.restaurantclosed.*

class RestaurantClosed : BaseActivity(){

    private lateinit var binding: RestaurantclosedBinding


    companion object {

        val TAG = "RestaurantClosed"
        fun newInstance() : RestaurantClosed {
            return RestaurantClosed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.restaurantclosed)
        initView(savedInstanceState)

    }

    private fun initView(savedInstanceState: Bundle?) {
        loge(TAG,""+intent.extras.get(Constants.MESSAGE_TITLE)+" -- "+intent.extras.get(Constants.MESSAGE_DETAILS))
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        msg_title.text=intent.extras.get(Constants.MESSAGE_TITLE).toString()
        msg_detail.text=intent.extras.get(Constants.MESSAGE_DETAILS).toString()

    }

    override fun onBackPressed() {
      //  super.onBackPressed()
      //  finish()
    }


}