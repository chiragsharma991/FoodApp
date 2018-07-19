package dk.eatmore.foodapp.fragment.ProductInfo

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.GenericAdapter
import dk.eatmore.foodapp.adapter.ViewHolderFactory
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.rating_restaurant.*

class Rating : BaseFragment() {

    private lateinit var binding: FragmentAccountContainerBinding



    companion object {

        val TAG = "Rating"
        fun newInstance(): Rating {
            return Rating()
        }

    }


    override fun getLayout(): Int {
        return R.layout.rating_restaurant
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)

        //   binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        // return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            val list= listOf<Any>(User("05/06/2018","charisharma16@gmail.com"),User("02/09/2018","monti@gmail.com"),User("22/03/2018","shayanti@gmail.com"))

            val mAdapter=object : GenericAdapter<Any>(list){
                override fun getLayoutId(position: Int, obj: Any): Int {
                    return when(obj){
                        is User -> R.layout.row_rating_list
                        else -> R.layout.row_rating_list
                    }
                }
                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return ViewHolderFactory.create(view,viewType,object : ViewHolderFactory.Clicklistner {
                        override fun clickOn() {


                        }

                    })
                }
            }
            recycler_view.layoutManager = LinearLayoutManager(getActivityBase())
            recycler_view.adapter = mAdapter


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



