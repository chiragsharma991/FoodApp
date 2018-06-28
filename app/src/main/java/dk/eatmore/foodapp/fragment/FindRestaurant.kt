package dk.eatmore.foodapp.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FindrestaurantBinding
import dk.eatmore.foodapp.utils.BaseFragment
import android.widget.Toast


class FindRestaurant : BaseFragment() {

    private lateinit var binding: FindrestaurantBinding
    val context : android.support.v4.app.FragmentActivity? = activity
    lateinit var clickEvent : MyClickHandler
    companion object {

        val TAG= "FindRestaurant"
        fun newInstance() : FindRestaurant {
            return FindRestaurant()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.findrestaurant
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        clickEvent =MyClickHandler(this)
        binding.handlers=clickEvent


    }

    override fun onDestroy() {
        super.onDestroy()
        log(TAG,"on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        log(TAG,"on detech...")

    }

    override fun onPause() {
        super.onPause()
        log(TAG,"on pause...")

    }


    inner class  MyClickHandler(internal var findrestaurant: FindRestaurant) {



         fun onFindClicked(view: View) {
             Toast.makeText(findrestaurant.activity, "Button long pressed!", Toast.LENGTH_SHORT).show();
             Log.e("click","---")
         }

    }







}