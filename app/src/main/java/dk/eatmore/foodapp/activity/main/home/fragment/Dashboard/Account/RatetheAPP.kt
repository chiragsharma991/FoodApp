package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerClickListner
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentOrderContainerBinding
import dk.eatmore.foodapp.databinding.RateOrderBinding
import dk.eatmore.foodapp.databinding.RateTheAppBinding
import dk.eatmore.foodapp.databinding.RowAttachedEmojisBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedRestaurant
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.rate_the_app.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.ArrayList

class RatetheAPP : BaseFragment() {


    private lateinit var binding: RateTheAppBinding
    private val ratedImage = ArrayList<RateModel>()
    private var rate_count=0
    var mAdapter: UniversalAdapter<RateModel, RowAttachedEmojisBinding>? = null


    companion object {
        val TAG = "RateOrder"
        fun newInstance(): RatetheAPP {
            val fragment = RatetheAPP()
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.rate_the_app
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        loge(TAG, "saveInstance " + savedInstanceState)
        if (savedInstanceState == null) {
            txt_toolbar.text = getString(R.string.bed_m_app)
            img_toolbar_back.setImageResource(R.drawable.back)
            img_toolbar_back.setOnClickListener { (activity as HomeActivity).onBackPressed() }
            send_btn.setOnClickListener{submit_rate()}
            ratedImage.add(RateModel(image = R.mipmap.rate_emoji1, is_selected = false, position = 0))
            ratedImage.add(RateModel(image = R.mipmap.rate_emoji2, is_selected = false, position = 1))
            ratedImage.add(RateModel(image = R.mipmap.rate_emoji3, is_selected = false, position = 2))
            ratedImage.add(RateModel(image = R.mipmap.rate_emoji4, is_selected = false, position = 3))
            ratedImage.add(RateModel(image = R.mipmap.rate_emoji5, is_selected = false, position = 4))
            refreshview()
        }

    }

    fun refreshview() {
        updatebutton()
        loge(TAG,"refresh--"+ratedImage[0].is_selected )
        recycler_view.apply {
            mAdapter = UniversalAdapter(context!!, ratedImage, R.layout.row_attached_emojis, object : RecyclerCallback<RowAttachedEmojisBinding, RateModel> {
                override fun bindData(binder: RowAttachedEmojisBinding, model: RateModel) {
                    binder.ratemodel=model
                    Glide.with(context!!)
                            .load(model.image)
                            .apply(RequestOptions().error(BindDataUtils.getRandomDrawbleColor()))
                            .into(binder.imageview)
                    binder.imageview.setOnClickListener {
                        loge(TAG,"click---")
                        for (i in 0.until(ratedImage.size)) {
                            if (i == model.position) {
                                ratedImage[i].is_selected = true
                                rate_count=i+1
                            } else {
                                ratedImage[i].is_selected = false
                            }
                        }
                        mAdapter!!.notifyDataSetChanged()
                        updatebutton()
                    }
                    binder.executePendingBindings()
                    //   binder.handler=this@OrderFragment
                }
            })
            layoutManager = LinearLayoutManager(getActivityBase(), LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
        }


    }


    fun submit_rate() {

        showProgressDialog()
        val postParam = getDefaultApiParms()
        postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN, ""))
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.COMMENT,comment_edt.text.trim().toString())
        postParam.addProperty(Constants.RATE,rate_count)

        callAPI(ApiCall.app_rating(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                showProgressDialog()
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    Toast.makeText(context, jsonObject.get(Constants.MSG).asString, Toast.LENGTH_SHORT).show()
                    (activity as HomeActivity).onBackPressed()
                } else {
                    showSnackBar(ratetheapp_container, jsonObject.get(Constants.MSG).asString)
                }

            }

            override fun onFail(error: Int) {
                showProgressDialog()
                when (error) {
                    404 -> {
                        showSnackBar(ratetheapp_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(ratetheapp_container, getString(R.string.internet_not_available))
                    }
                }
            }
        })
    }





    fun updatebutton(){
        for (i in 0.until(ratedImage.size)) {
            if (ratedImage[i].is_selected) {
                send_btn.isEnabled=true
                send_btn.alpha=1.0f
                return
            }
        }

        send_btn.isEnabled=false
        send_btn.alpha=0.5f
    }


    data class RateModel(var image: Int, var is_selected: Boolean, val position: Int)


    override fun onDestroy() {
        super.onDestroy()
        loge(TAG, "on destroy...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loge(TAG, "on destroyView...")
        // ui_model=null
    }

    override fun onDetach() {
        super.onDetach()
        loge(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        loge(TAG, "on pause...")

    }

}












