package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.utils.BaseFragment
import android.support.v7.widget.LinearLayoutManager
import com.facebook.internal.Mutable
import com.google.gson.JsonObject
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.menu_restaurant.*
import kotlinx.android.synthetic.main.openinghours.*
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList


class OpeningHours : BaseFragment() {


    private lateinit var binding: OpeninghoursBinding
    private lateinit var mAdapter: UniversalAdapter<OpeningHourModel, RowOpeningHoursBinding>
    private lateinit var openinghourmodel: OpeningHourModel
    private  var openinghoursList :ArrayList<OpeningHourModel> = ArrayList()



    companion object {

        val TAG = "OpeningHours"
        fun newInstance(): OpeningHours {
            return OpeningHours()
        }

    }


    override fun getLayout(): Int {
        return R.layout.openinghours
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            fetch_OpeningHours()
            mAdapter = UniversalAdapter(context!!,openinghoursList, R.layout.row_opening_hours, object : RecyclerCallback<RowOpeningHoursBinding, OpeningHourModel> {
                override fun bindData(binder: RowOpeningHoursBinding, model: OpeningHourModel) {
                    setRecyclerData(binder, model)
                }
            })
            recyclerview_list.layoutManager = LinearLayoutManager(getActivityBase())
            recyclerview_list.adapter = mAdapter


        }else{
            logd(TAG,"saveInstance NOT NULL")


        }
    }

    private fun setRecyclerData(binder: RowOpeningHoursBinding, model: OpeningHourModel) {
        //binder.data=model


    }

    private fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                viewcard_list.observe(this@OpeningHours, android.arch.lifecycle.Observer<OpeningHourModel> {
                  //  refresh_viewCard()
                })
            }


    class UIModel : ViewModel() {

        var viewcard_list = MutableLiveData<OpeningHourModel>()

    }

    private fun fetch_OpeningHours() {

        callAPI(ApiCall.openingHours(
                r_token = Constants.R_TOKEN,
                r_key = Constants.R_KEY
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json= body as JsonObject
                try {
                    val status = json.get("status").asBoolean
                    // Check for error node in json
                    if (status) {
                        val calendar = Calendar.getInstance()
                        val day = calendar.get(Calendar.DAY_OF_WEEK)

                        val jsonArray = json.getAsJsonArray("Openinghours")
                        for (i in 0 until jsonArray.size()) {
                            val jsonObject = jsonArray.get(i)
//jsonObject.asJsonObject.get("closes").asString
                            if (i == day - 2)
                               openinghourmodel = OpeningHourModel(jsonObject.asJsonObject.get("day").asString,
                                        jsonObject.asJsonObject.get("opens").asString + "   -   " + jsonObject.asJsonObject.get("closes").asString , true)
                            else
                                openinghourmodel =  OpeningHourModel(jsonObject.asJsonObject.get("day").asString,
                                        jsonObject.asJsonObject.get("opens").asString + "   -   " + jsonObject.asJsonObject.get("closes").asString, false)

                            openinghoursList.add(openinghourmodel)
                            //    Log.e("Calendar","Calendar"+day);
                        }
                        mAdapter.notifyDataSetChanged()
                    }

                } catch (e: JSONException) {
                    // hideDialog();
                    e.printStackTrace()
                }


            }
            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(clayout_menu, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout_menu, getString(R.string.internet_not_available))
                    }
                }
            }
        })


    }





    fun setToolbarforThis(){

    }

    fun onBackpress(){


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


    data class OpeningHourModel(var openingDays: String, var openingTime: String, var openingFlag: Boolean?)


}
