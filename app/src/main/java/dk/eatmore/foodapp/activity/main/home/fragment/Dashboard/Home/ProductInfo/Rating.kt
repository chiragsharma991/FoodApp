package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo

import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.AccountFragment
import dk.eatmore.foodapp.adapter.GenericAdapter
import dk.eatmore.foodapp.adapter.ViewHolderFactory
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerClickListner
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.databinding.RatingRestaurantBinding
import dk.eatmore.foodapp.databinding.RowMenuRestaurantBinding
import dk.eatmore.foodapp.databinding.RowRatingListBinding
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.Rating_details
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.Review_list
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.menu_restaurant.*
import kotlinx.android.synthetic.main.rating_restaurant.*

class Rating : BaseFragment(), RecyclerClickListner {


    private lateinit var binding: RatingRestaurantBinding
    private lateinit var restaurant : Restaurant
    private lateinit var clickEvent: MyClickHandler
    private var mAdapter: UniversalAdapter<Review_list, RowRatingListBinding>? = null




    companion object {


        val TAG = "Rating"
        fun newInstance(restaurant : Restaurant): Rating {
            val fragment = Rating()
            val bundle = Bundle()
            bundle.putSerializable(Constants.RESTAURANT,restaurant)
            fragment.arguments = bundle
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.rating_restaurant
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(getLayout(), container, false)
           binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
           return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            getlength()
            restaurant= arguments!!.getSerializable(Constants.RESTAURANT) as Restaurant
            clickEvent = MyClickHandler(this)
            binding.restaurant=restaurant
            binding.handler=clickEvent
            binding.util=BindDataUtils
            mAdapter = UniversalAdapter(context!!,restaurant.review_list, R.layout.row_rating_list, object : RecyclerCallback<RowRatingListBinding, Review_list> {
                override fun bindData(binder: RowRatingListBinding, model: Review_list) {
                    setRecyclerData(binder, model)
                }
            })
            recycler_view.layoutManager = LinearLayoutManager(getActivityBase())
            recycler_view.adapter = mAdapter

        }else{
            logd(TAG,"saveInstance NOT NULL")

        }


    }

    override fun <T> onClick(model: T?) {


    }


    private fun setRecyclerData(binder: RowRatingListBinding, model: Review_list) {
        binder.reviewList=model
        binder.handler=this
    }

    private fun getlength(){

            val observer = rating_line.getViewTreeObserver()
            observer.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val headerLayoutHeight = rating_line.getHeight()
                    val headerLayoutWidth = rating_line.getWidth()
                    val density = context!!.getResources().displayMetrics.density
                    val px = 100 * density
                    val dp = 100 / density
                    loge("view length is ",""+headerLayoutHeight+" "+headerLayoutWidth+" "+px+" "+dp)
                    view06.layoutParams.width=0
                    view06.requestLayout()
                    rating_line.getViewTreeObserver().removeGlobalOnLayoutListener(
                            this)
                }
            })

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


    class MyClickHandler(val rating: Rating) {


        fun tapOnRating(view: View) {
            rating.tapOnRating()
        }


    }

    private fun tapOnRating(){

    }






}


@BindingAdapter("android:layout_calculatewidth")
fun calculateRatingline(view : View, percent : Int) {
    // i set 100 fixed dp in rating page thats why i am using 100
    val density = view.context!!.getResources().displayMetrics.density
    val px = 100.00 * density
    val dp = 100.00 / density
    val length =px
    val result : Double = ((percent.toDouble()/100) *length)
    view.layoutParams.width=result.toInt()
    view.requestLayout()

}



