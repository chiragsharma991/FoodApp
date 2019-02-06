package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account


import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.utils.BaseFragment
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.rest.ApiClient
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.restpaymentmethods.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call


class RestPaymentMethods : BaseFragment() {


    private lateinit var binding: RestpaymentmethodsBinding
    private var call_restaurant_payment_method : Call<RestPayMethodModel>? = null
    private var ui_model: UIModel? = null




    companion object {
        // id =0 (termsofservices) || id =1 (cokkie_policy)
        val TAG = "RestPaymentMethods"
        fun newInstance( ): RestPaymentMethods {
            return RestPaymentMethods()
        }

    }


    override fun getLayout(): Int {
        return R.layout.restpaymentmethods
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            progress_bar.visibility=View.GONE
            txt_toolbar.text="Betalingsmetoder"
            img_toolbar_back.setOnClickListener{(activity as HomeActivity).onBackPressed()}
            ui_model = createViewModel()
            fetchuserInfo()
        }
    }

    class UIModel : ViewModel() {
        var restpaymethod_list = MutableLiveData<RestPayMethodModel>()
    }


    private fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                restpaymethod_list.removeObservers(this@RestPaymentMethods)
                restpaymethod_list.observe(this@RestPaymentMethods, Observer<RestPayMethodModel> {
                    refreshview()
                })
            }

    fun fetchuserInfo() {
        progress_bar.visibility=View.VISIBLE
        val postParam = getDefaultApiParms()
        call_restaurant_payment_method= ApiCall.restaurant_payment_method(jsonObject = postParam)
        callAPI(call_restaurant_payment_method!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val restpaymethodmodel = body as RestPayMethodModel
                if (restpaymethodmodel.status) {
                    for (i in 0 until restpaymethodmodel.data.size){
                        restpaymethodmodel.data[i].copy_logo=restpaymethodmodel.payment_method_thumbnail_logo+""+restpaymethodmodel.data[i].logo
                    }
                    ui_model!!.restpaymethod_list.value=restpaymethodmodel
                    progress_bar.visibility=View.GONE
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(payment_method_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(payment_method_container, getString(R.string.internet_not_available))
                    }
                }
                progress_bar.visibility=View.GONE
            }
        })
    }



    private fun refreshview(){
        loge(TAG,"refresh view...")
        val mAdapter = UniversalAdapter(context!!, ui_model!!.restpaymethod_list.value!!.data, R.layout.row_restpaymethod, object : RecyclerCallback<RowRestpaymethodBinding, Data> {
            override fun bindData(binder: RowRestpaymethodBinding, model: Data) {
                binder.data=model
                Glide.with(context!!)
                        .load(model.copy_logo)
                        .apply(RequestOptions().error(BindDataUtils.getRandomDrawbleColor()))
                        .into(binder.image)
                binder.executePendingBindings()
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view.adapter = mAdapter
    }


     data class RestPayMethodModel (
            val status: Boolean = false,
            val msg: String ="",
            val payment_method_image_path: String ="",
            val payment_method_thumbnail_logo: String ="",
            val data: ArrayList<Data>
    )
  /*  {
        constructor (status: Boolean?,msg: String?,payment_method_image_path: String?,payment_method_thumbnail_logo: String?,data: ArrayList<Data>?): this(status,"","","", arrayListOf())
    }*/
    data class Data (
          val pm_name: String ="",
          val logo: String ="",
          var copy_logo: String =""

    )


    override fun onDestroyView() {

        super.onDestroyView()

        logd(TAG, "onDestroyView...")

        ui_model?.let {
            ViewModelProviders.of(this).get(UIModel::class.java).restpaymethod_list.removeObservers(this@RestPaymentMethods)
        }

        if (call_restaurant_payment_method != null) {
            call_restaurant_payment_method!!.cancel()
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
