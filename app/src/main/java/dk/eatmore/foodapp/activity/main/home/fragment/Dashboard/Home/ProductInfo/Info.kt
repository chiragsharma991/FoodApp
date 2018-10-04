package dk.eatmore.foodapp.activity.main.home.fragment.ProductInfo

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.OpeningHours
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.databinding.InfoRestaurantBinding
import dk.eatmore.foodapp.databinding.RowOpeningHoursBinding
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.info_restaurant.*
import kotlinx.android.synthetic.main.menu_restaurant.*
import kotlinx.android.synthetic.main.openinghours.*
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

class Info : BaseFragment() {

    private lateinit var binding: InfoRestaurantBinding
    private lateinit var mAdapter: UniversalAdapter<OpeningHourModel, RowOpeningHoursBinding>
    private lateinit var openinghourmodel: OpeningHourModel
    private val openinghoursList: ArrayList<OpeningHourModel> =ArrayList()
    var ui_model: Info.UIModel? = null



    companion object {

        val TAG = "Info"
        fun newInstance(): Info {
            return Info()
        }

    }


    override fun getLayout(): Int {
        return R.layout.info_restaurant
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        loge(TAG, "create view...")
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            ui_model = createViewModel()
            fetch_OpeningHours()

        } else {
            logd(TAG, "saveInstance NOT NULL")

        }
    }

    private fun setRecyclerData(binder: RowOpeningHoursBinding, model: OpeningHourModel) {
        binder.data = model
    }


    private fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                openinghoursList.observe(this@Info, android.arch.lifecycle.Observer<ArrayList<OpeningHourModel>> {
                      refresh_view()
                })
            }

    class UIModel : ViewModel() {

        var openinghoursList = MutableLiveData<ArrayList<OpeningHourModel>>()

    }

    private fun refresh_view() {

        mAdapter = UniversalAdapter(context!!, ui_model!!.openinghoursList.value, R.layout.row_opening_hours, object : RecyclerCallback<RowOpeningHoursBinding, OpeningHourModel> {
            override fun bindData(binder: RowOpeningHoursBinding, model: OpeningHourModel) {
                setRecyclerData(binder, model)
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view.adapter = mAdapter

    }


    private fun fetch_OpeningHours() {

        callAPI(ApiCall.openingHours(
                r_token = Constants.R_TOKEN,
                r_key = Constants.R_KEY
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject
                try {
                    openinghoursList.clear()
                    val status = json.get("status").asBoolean
                    if (status) {
                        val calendar = Calendar.getInstance()
                        val day = calendar.get(Calendar.DAY_OF_WEEK)

                        val jsonArray = json.getAsJsonArray("Openinghours")
                        for (i in 0 until jsonArray.size()) {
                            val jsonObject = jsonArray.get(i)
                            if (i == day - 2)
                                openinghourmodel = OpeningHourModel(jsonObject.asJsonObject.get("day").asString,
                                        jsonObject.asJsonObject.get("opens").asString + "   -   " + jsonObject.asJsonObject.get("closes").asString, true)
                            else
                                openinghourmodel = OpeningHourModel(jsonObject.asJsonObject.get("day").asString,
                                        jsonObject.asJsonObject.get("opens").asString + "   -   " + jsonObject.asJsonObject.get("closes").asString, false)

                            openinghoursList.add(openinghourmodel)
                            //    Log.e("Calendar","Calendar"+day);
                        }
                        ui_model!!.openinghoursList.value=openinghoursList  //notify data
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(clayout, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout, getString(R.string.internet_not_available))
                    }
                }
            }
        })


    }

    data class OpeningHourModel(var openingDays: String, var openingTime: String, var openingFlag: Boolean?)


    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
       // ui_model!!.openinghoursList.value!!.clear()
        logd(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }

}



