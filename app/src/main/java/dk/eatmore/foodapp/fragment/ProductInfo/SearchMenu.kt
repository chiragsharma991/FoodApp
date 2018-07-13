package dk.eatmore.foodapp.fragment.ProductInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.Main.HomeActivity
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.DrawableClickListener.DrawablePosition
import dk.eatmore.foodapp.utils.DrawableClickListener
import kotlinx.android.synthetic.main.search_menu.*


class SearchMenu : BaseFragment() {

    private lateinit var binding: FragmentAccountContainerBinding



    companion object {

        val TAG = "SearchMenu"
        fun newInstance(): SearchMenu {
            return SearchMenu()
        }

    }


    override fun getLayout(): Int {
        return R.layout.search_menu
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)

        //   binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        // return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            search_edt.requestFocus()
            showKeyboard()
            search_edt.setDrawableClickListener(object : DrawableClickListener {


                override fun onClick(target: DrawablePosition) {
                    when (target) {
                        DrawableClickListener.DrawablePosition.LEFT -> {
                            (activity as HomeActivity).onBackPressed()
                        }

                        else -> {
                            search_edt.text.clear()
                        }
                    }//Do something here
                }

            })


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

