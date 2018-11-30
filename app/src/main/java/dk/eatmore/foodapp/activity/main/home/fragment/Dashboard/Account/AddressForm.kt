package dk.eatmore.foodapp.fragment.Dashboard.Home

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.Editable
import android.text.TextWatcher
import android.transition.Transition
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.EditAddress
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.Profile
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.databinding.RagistrationFormBinding
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.ragistration_form.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class AddressForm : BaseFragment(), TextWatcher {


    var transition : Transition?=null
    private val userList = ArrayList<User>()
    private  var mAdapter: CartViewAdapter?=null
    private lateinit var binding: RagistrationFormBinding
    private lateinit var homeFragment: HomeFragment
    private lateinit var address: EditAddress.Messages
    private val inputValidStates = HashMap<EditText, Boolean>()
    private var postalcity: LinkedHashMap<String, String> ?=null





    companion object {
        val TAG="AddressForm"
        fun newInstance(address: EditAddress.Messages) : AddressForm {
            val fragment = AddressForm()
            val bundle =Bundle()
            bundle.putSerializable(Constants.ADDRESS,address)
            fragment.arguments=bundle
            return fragment
        }
    }

    override fun getLayout(): Int {
        return R.layout.ragistration_form
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            txt_toolbar.text=getString(R.string.edit_address)
            img_toolbar_back.setImageResource(R.drawable.close)
            img_toolbar_back.setOnClickListener{(activity as HomeActivity).onBackPressed()}
            //   setToolbarforThis()
            if(RestaurantList.ui_model !=null){
                // Add postal code if restaurant list is open anotherwise null
                loge(TAG,"postal size is-"+ RestaurantList.ui_model!!.restaurantList.value!!.postal_city.size.toString())
                postalcity =LinkedHashMap<String,String>()
                for(i in 0 until RestaurantList.ui_model!!.restaurantList.value!!.postal_city.size){
                    postalcity!!.put(RestaurantList.ui_model!!.restaurantList.value!!.postal_city[i].postal_code,RestaurantList.ui_model!!.restaurantList.value!!.postal_city[i].city_name)
                }
            }
            address= arguments!!.getSerializable(Constants.ADDRESS) as EditAddress.Messages
            loge(TAG,"city is"+address.city)
            binding.address=address

            Handler().postDelayed({
                city_edt.addTextChangedListener(this)
                postnumber_edt.addTextChangedListener(this)
                street_edt.addTextChangedListener(this)
                house_edt.addTextChangedListener(this)
                inputValidStates[postnumber_edt] = true
                inputValidStates[street_edt] = true
                inputValidStates[house_edt] = true
                inputValidStates[city_edt] = true
            },200)

            postnumber_edt.imeOptions= EditorInfo.IME_ACTION_DONE
            postnumber_edt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    moveon_save()
                    return true
                }
            })
            save.setOnClickListener {
                moveon_save()
            }


        }else{

        }


    }

    fun validationFields(): Boolean {
        var isvalidate: Boolean = true
        // Test validtion between pickup/delivery
            if (!inputValidStates[postnumber_edt]!!) {
                postnumber_edt.error = getString(R.string.enter_your_postal_number)
                isvalidate = false
            }
            if (!inputValidStates[street_edt]!!) {
                street_edt.error = getString(R.string.enter_your_street_number)
                isvalidate = false
            }
            if (!inputValidStates[house_edt]!!) {
                house_edt.error = getString(R.string.enter_your_house_number)
                isvalidate = false
            }
            if (city_edt.text.trim().length <= 0) {
                postnumber_edt.error = getString(R.string.postal_number_is_not_valid)
                isvalidate = false
            }
        return isvalidate
    }


    fun moveon_save(){

        if (validationFields()) {
            saveuserInfo(address.id)
        }
    }


    override fun afterTextChanged(s: Editable?) {
        loge(TAG,"after text changed--")
        if (postnumber_edt.text.hashCode() == s!!.hashCode()) {
            postnumber_edt.error = null
            if (postnumber_edt.text.trim().toString().length > 0) {
                loge(TAG,"text changed postnumber...")
                inputValidStates[postnumber_edt] = true
                if(postalcity ==null)
                city_edt.setText("")
                else
                city_edt.setText(postalcity!!.get(postnumber_edt.text.trim().toString()))
            } else
                inputValidStates[postnumber_edt] = false

        } else if (street_edt.text.hashCode() == s.hashCode()) {
            street_edt.error = null
            if (street_edt.text.trim().toString().length > 0)
                inputValidStates[street_edt] = true
            else
                inputValidStates[street_edt] = false

        } else if (house_edt.text.hashCode() == s.hashCode()) {
            house_edt.error = null
            if (house_edt.text.trim().toString().length > 0)
                inputValidStates[house_edt] = true
            else
                inputValidStates[house_edt] = false

        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }


    private fun saveuserInfo(id : String) {

        showProgressDialog()
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP,true)
        postParam.addProperty(Constants.ID, id)
        postParam.addProperty(Constants.IS_LOGIN, "1")
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.STREET, street_edt.text.trim().toString())
        postParam.addProperty(Constants.HOUSE_NO, house_edt.text.trim().toString())
        postParam.addProperty(Constants.FLOOR_DOOR, floor_edt.text.trim().toString())
        postParam.addProperty(Constants.POSTAL_CODE, postnumber_edt.text.trim().toString())
        postParam.addProperty(Constants.CITY, city_edt.text.trim().toString())
        postParam.addProperty(Constants.ADDRESS_TITLE, address_title.text.trim().toString())
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then

        callAPI(ApiCall.edit_shippingaddress(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    Toast.makeText(context,jsonObject.get(Constants.MSG).asString, Toast.LENGTH_SHORT).show()
                    val fragment =(parentFragment as Profile).childFragmentManager.findFragmentByTag(EditAddress.TAG)
                    (fragment as EditAddress).fetchuserInfo()
                    (activity as HomeActivity).onBackPressed()
                }
                showProgressDialog()
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(edit_address_container, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(edit_address_container, getString(R.string.internet_not_available))
                    }
                }
                showProgressDialog()
            }
        })
    }




    fun setToolbarforThis(){
        (activity as EpayActivity).txt_toolbar.text=getString(R.string.add_new_address)
        (activity as EpayActivity).txt_toolbar_right.text=getString(R.string.ok)
        (activity as EpayActivity).img_toolbar_back.setOnClickListener{ onBackpress() }
        (activity as EpayActivity).txt_toolbar_right.setOnClickListener{

        }


    }

    fun onBackpress(){
        if(this.isVisible){
            (parentFragment as Address).setToolbarforThis()
            (parentFragment as Address).popFragment()
        }
    }




    }
