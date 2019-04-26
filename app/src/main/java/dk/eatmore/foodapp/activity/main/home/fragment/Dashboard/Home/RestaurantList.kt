package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home


import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.transition.ChangeBounds
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.filter.KokkenType
import dk.eatmore.foodapp.activity.main.filter.SearchRestaurant
import dk.eatmore.foodapp.activity.main.filter.Tilpas
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.restaurantList.RestaurantListParentAdapter
import dk.eatmore.foodapp.databinding.RestaurantlistBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.ModelUtility
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.restaurantlist.*
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class RestaurantList : SearchRestaurant(), TextWatcher {


    private lateinit var binding: RestaurantlistBinding
    private lateinit var clickEvent: MyClickHandler
    private lateinit var list: ArrayList<StatusWiseRestaurant>
    private  var mAdapter: RestaurantListParentAdapter? = null
    private var call_restaurantlist: Call<RestaurantListModel>? = null
    private val overall_cuisines_list: ArrayList<String> = ArrayList()
    private  var restaurantlistmodel: RestaurantListModel? =null
    private  var filterable_restaurantlistmodel: RestaurantListModel?=null
    private val kokkenType_map: HashMap<String, Int> = HashMap()
    private var is_from_filter : Boolean = false
    private var call_favorite: Call<JsonObject>? = null



    companion object {

        fun getuimodel(): UIModel? {
            return ui_model
        }

        val TAG = "RestaurantList"
        var ui_model: RestaurantList.UIModel? = null
        fun newInstance(postal_code: String, is_fav : Boolean): RestaurantList {
            val fragment = RestaurantList()
            val bundle = Bundle()
            bundle.putString(Constants.POSTAL_CODE, postal_code)
            bundle.putBoolean(Constants.IS_FAV, is_fav)
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun getLayout(): Int {
        return R.layout.restaurantlist
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            error_view.visibility = View.GONE
            HomeFragment.is_from_reorder=false
            clickEvent = MyClickHandler(this)
            binding.handler = clickEvent
            setToolbarforThis()
            search_edt.addTextChangedListener(this)
            search_again_btn.setOnClickListener {
                ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().childFragmentManager.popBackStack()
            }
            ui_model = createViewModel()

            if (ui_model!!.restaurantList.value == null) {
                fetch_ProductDetailList()
            } else {
                refreshview()
            }
        } else {
            logd(TAG, "saveInstance NOT NULL")
        }

    }

    override fun afterTextChanged(s: Editable?) {
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        app_bar.setExpanded(true) }

    fun setToolbarforThis() {
        progress_bar.visibility = View.GONE
        toolbar.visibility = View.VISIBLE
        search_tool.visibility = View.GONE
        txt_toolbar.text =if(HomeFragment.is_fav) getString(R.string.favourite_restauranter) else getString(R.string.restaurants)
        error_txt.text =if(HomeFragment.is_fav) getString(R.string.ingen_favorit_restauranter) else getString(R.string.ingen_restauranter_prøv_en_anden_søgning)
        search_again_btn.text =if(HomeFragment.is_fav) getString(R.string.søg_efter_restaurnter) else getString(R.string.søg_igen)
        img_toolbar_back.setImageResource(R.drawable.back)
        img_toolbar_back.setOnClickListener {
            onBackpress()
        }

        //-- search list --//

        search_clear_btn.setOnClickListener{
            search_edt.text.clear()
        }

        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
                is_from_filter=true
                searchRestaurantList(search_edt.text.toString(),filterable_restaurantlistmodel)
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int,
                                           arg2: Int, arg3: Int) {
            }

            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int,
                                       arg3: Int) {
            }
        })

        search_tool_cancel.setOnClickListener{
          onBackpress()
        }
    }


    fun fetch_ProductDetailList() {
        progress_bar.visibility = View.VISIBLE
        val bundle = arguments
        val jsonobject = JsonObject()
        jsonobject.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        jsonobject.addProperty(Constants.EATMORE_APP, true)
        if(bundle!!.getBoolean(Constants.IS_FAV)){
            // if you fav . then add this line.
            jsonobject.addProperty(Constants.IS_FAV, true)
            HomeFragment.is_fav=false
        }
        jsonobject.addProperty(Constants.POSTAL_CODE, bundle.getString(Constants.POSTAL_CODE, ""))
        if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
            jsonobject.addProperty(Constants.IS_LOGIN, "1")
            jsonobject.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        } else {
            jsonobject.addProperty(Constants.IS_LOGIN, "0")
        }
        jsonobject.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN, ""))
        jsonobject.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        call_restaurantlist = ApiCall.restaurantList(jsonobject)
        callAPI(call_restaurantlist!!, object : BaseFragment.OnApiCallInteraction {

            //private var items: MutableList<List<String>>? =null

            override fun <T> onSuccess(body: T?) {
                restaurantlistmodel = body as RestaurantListModel
                if (restaurantlistmodel!!.status) {

                    /*Todo Kokken Type login
                    *   first we are passing cuisines list in all 3 list (open/close/pre) using split function to arraylist and calculating overall cuisines list
                    *   After this we pass all list in map to add count and remove dublication and prepair orderlist.
                    *   Crete one view model to communication between fragment to activity
                    *   first i added view model and pass to another activity and filter using our condition.
                    * */


                    for (i in 0 until 3) {
                        when (i) {
                            0 -> {
                                for (j in 0 until restaurantlistmodel!!.restaurant_list.open_now.size) {
                                    restaurantlistmodel!!.restaurant_list.open_now[j].is_open_now=true
                                    if (restaurantlistmodel!!.restaurant_list.open_now[j].cuisines.length > 0) {
                                        val items: Array<String> = restaurantlistmodel!!.restaurant_list.open_now[j].cuisines.split((",").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                        for (item: String in items) {
                                            restaurantlistmodel!!.restaurant_list.open_now.get(j).cuisines_list.add(item.trim())
                                            overall_cuisines_list.add(item)
                                        }
                                    }

                                }
                            }
                            1 -> {
                                for (k in 0 until restaurantlistmodel!!.restaurant_list.pre_order.size) {
                                    if (restaurantlistmodel!!.restaurant_list.pre_order[k].cuisines.length > 0) {
                                        val items: Array<String> = restaurantlistmodel!!.restaurant_list.pre_order[k].cuisines.split((",").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                        for (item: String in items) {
                                            restaurantlistmodel!!.restaurant_list.pre_order.get(k).cuisines_list.add(item.trim())
                                            overall_cuisines_list.add(item)
                                        }
                                    }
                                }
                            }
                            2 -> {
                                for (l in 0 until restaurantlistmodel!!.restaurant_list.closed.size) {
                                    if (restaurantlistmodel!!.restaurant_list.closed[l].cuisines.length > 0) {
                                        val items: Array<String> = restaurantlistmodel!!.restaurant_list.closed[l].cuisines.split((",").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                        for (item: String in items) {
                                            restaurantlistmodel!!.restaurant_list.closed.get(l).cuisines_list.add(item.trim())
                                            overall_cuisines_list.add(item)
                                        }
                                    }
                                }
                            }
                        }


                    }


                    val overall_restaurentlist: Int = restaurantlistmodel!!.restaurant_list.open_now.size + restaurantlistmodel!!.restaurant_list.pre_order.size + restaurantlistmodel!!.restaurant_list.closed.size
                    for (j in 0 until overall_cuisines_list.size) {
                        val value = overall_cuisines_list.get(j).trim()
                        var count = 0
                        for (x in 0 until overall_cuisines_list.size) {
                            if (overall_cuisines_list.get(x).trim() == value) {
                                count++
                                kokkenType_map.put(value, count)
                            }
                        }

                    }
                    kokkenType_map.put(getString(R.string.all), overall_restaurentlist)
                    for (key in kokkenType_map.keys) {
                        val value = kokkenType_map.get(key)!!.toInt()
                        ui_model!!.kokkenType_list.add(kokken_Model(itemcount = value, itemtype = key))
                    }
                    Collections.sort(ui_model!!.kokkenType_list, object : Comparator<kokken_Model> {
                        override fun compare(lhs: kokken_Model, rhs: kokken_Model): Int {
                            return lhs.itemtype.compareTo(rhs.itemtype)
                        }
                    })
                    for (i in 0 until ui_model!!.kokkenType_list.size) {
                        if (ui_model!!.kokkenType_list.get(i).itemtype.trim() == getString(R.string.all).trim())
                            ui_model!!.kokkenType_list.get(i).is_itemselected = true
                    }

                    // Added Tilpas sort list:
                    ui_model!!.tilpassort_list.add(kokken_Model(itemtype = "Gratis Levering"))
                    ui_model!!.tilpassort_list.add(kokken_Model(itemtype = "5+ Stjerner"))
                    ui_model!!.tilpassort_list.add(kokken_Model(itemtype = "Abent Nu"))
                    ui_model!!.tilpassort_list.add(kokken_Model(itemtype = "Hent Selv"))
                    ui_model!!.tilpassort_list.add(kokken_Model(itemtype = "Ny"))

                    // Added easy sort list
                    ui_model!!.easysort_list.add(kokken_Model(itemtype = "Papularitet"))
                    ui_model!!.easysort_list.add(kokken_Model(itemtype = "Leveringsgebyr"))
                    ui_model!!.easysort_list.add(kokken_Model(itemtype = "Minimum ordre"))
                    ui_model!!.easysort_list.add(kokken_Model(itemtype = "Nyeste forst"))
                    ui_model!!.easysort_list.add(kokken_Model(itemtype = "Navn"))


                    ui_model!!.restaurantList.value = restaurantlistmodel!!
                    filterable_restaurantlistmodel=ui_model!!.restaurantList.value

                }
            }

            override fun onFail(error: Int) {


                if (call_restaurantlist!!.isCanceled) {
                    return
                }

                when (error) {
                    404 -> {
                        showSnackBar(clayout_crt, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout_crt, getString(R.string.internet_not_available))
                    }
                }
                progress_bar.visibility = View.GONE

            }
        })


    }




    fun updatefavourite(){
        if(mAdapter !=null)
        mAdapter!!.notifyDataSetChanged()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loge("onActivityResult Restaurantlist---", "---")
        if (requestCode == Constants.REQ_FILTER_RESAURANT_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                filterable_restaurantlistmodel = data!!.getBundleExtra(Constants.BUNDLE).getSerializable(Constants.FILTER_RESTAURANTLISTMODEL) as RestaurantListModel
                if(search_tool.visibility == View.VISIBLE){
                    searchRestaurantList(search_edt.text.toString(),filterable_restaurantlistmodel)
                }else{
                    ui_model!!.restaurantList.value = filterable_restaurantlistmodel
                }

            }
        }else if(requestCode == Constants.REQ_SORT_RESAURANT_LIST){
            if (resultCode == Activity.RESULT_OK) {
                filterable_restaurantlistmodel = data!!.getBundleExtra(Constants.BUNDLE).getSerializable(Constants.FILTER_RESTAURANTLISTMODEL) as RestaurantListModel
                ui_model!!.restaurantList.value = filterable_restaurantlistmodel
                if(search_tool.visibility == View.VISIBLE){
                    searchRestaurantList(search_edt.text.toString(),filterable_restaurantlistmodel)
                }else{
                    ui_model!!.restaurantList.value = filterable_restaurantlistmodel
                }
            }
        }
    }


    override fun searchcompleted(list: RestaurantListModel) {
        // get result from search restaurant.
        loge(TAG,"searchcompleted---")
        ui_model!!.restaurantList.value = list
    }

    @Subscribe
    fun onEvent(parsingevents: ParsingEvents.EventItself) {
        loge(TAG, "--EventItself")
    }




    private fun refreshview() {
        loge(TAG, "refresh---")
        var statuswiserestaurant: StatusWiseRestaurant
        list = ArrayList()
        val open_now = ui_model!!.restaurantList.value!!.restaurant_list.open_now
        val pre_order = ui_model!!.restaurantList.value!!.restaurant_list.pre_order
        val closed = ui_model!!.restaurantList.value!!.restaurant_list.closed

        if (ui_model!!.restaurantList.value!!.restaurant_list.open_now.size > 0) {
            statuswiserestaurant = StatusWiseRestaurant(String.format(getString(R.string.open_now),open_now.size), getString(R.string.ordernow),open_now)
            list.add(statuswiserestaurant)
        }
        if (ui_model!!.restaurantList.value!!.restaurant_list.pre_order.size > 0) {
            statuswiserestaurant = StatusWiseRestaurant(String.format(getString(R.string.pre_order),pre_order.size), getString(R.string.preorder), pre_order)
            list.add(statuswiserestaurant)
        }
        if (ui_model!!.restaurantList.value!!.restaurant_list.closed.size > 0) {
            statuswiserestaurant = StatusWiseRestaurant(String.format(getString(R.string.closed),closed.size), getString(R.string.notavailable),closed)
            list.add(statuswiserestaurant)
        }
        if (list.size <= 0) {
            if(is_from_filter){
                error_view.visibility = View.VISIBLE
                error_txt.text =getString(R.string.didnot_match_restaurant_error)
                search_again_btn.visibility=View.GONE
              //  is_from_filter=false
            }else{
                error_view.visibility = View.VISIBLE
                error_txt.text =getString(R.string.unfortunately_error)
                search_again_btn.visibility=View.VISIBLE
            }
        } else {
            error_view.visibility = View.GONE
        }


        recycler_view_parent.apply {
            loge(TAG,"recycler_view_parent---")
            mAdapter = RestaurantListParentAdapter(context!!, list, object : RestaurantListParentAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int,tag : String) {

                    when(tag){
                        Constants.CARD_VIEW ->{

                            if (progress_bar.visibility == View.VISIBLE ) return
                            val fragment = DetailsFragment.newInstance(
                                    restaurant = list.get(parentPosition).restaurant.get(chilPosition),
                                    status = list.get(parentPosition).status,
                                    ordertype = list.get(parentPosition).ordertype
                            )
                            var enter: Slide? = null
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                enter = Slide()
                                enter.setDuration(300)
                                enter.slideEdge = Gravity.BOTTOM
                                val changeBoundsTransition: ChangeBounds = ChangeBounds()
                                changeBoundsTransition.duration = 300
                                //fragment!!.sharedElementEnterTransition=changeBoundsTransition
                                fragment.sharedElementEnterTransition = changeBoundsTransition
                                fragment.sharedElementReturnTransition = changeBoundsTransition
                                fragment.enterTransition = enter
                            }
                            PreferenceUtil.putValue(PreferenceUtil.R_KEY, list.get(parentPosition).restaurant.get(chilPosition).r_key)
                            PreferenceUtil.putValue(PreferenceUtil.R_TOKEN, list.get(parentPosition).restaurant.get(chilPosition).r_token)
                            PreferenceUtil.save()
                            (parentFragment as HomeFragment).addFragment(R.id.home_fragment_container, fragment, DetailsFragment.TAG, false)
                        }

                        Constants.FAVORITE_VIEW ->{
                            val postParam = JsonObject()
                            postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                            postParam.addProperty(Constants.EATMORE_APP, true)
                            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,""))      // if restaurant is closed then
                            postParam.addProperty(Constants.RESTAURANT_ID,list[parentPosition].restaurant[chilPosition].restaurant_id)
                            if(list[parentPosition].restaurant[chilPosition].is_fav){
                                // unfavourite--

                                DialogUtils.openDialog(context = context!!,btnNegative = getString(R.string.no) , btnPositive = getString(R.string.yes),color = ContextCompat.getColor(context!!, R.color.theme_color),msg = getString(R.string.vil_du_fjerne),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                                    override fun onPositiveButtonClick(position: Int) {
                                        list[parentPosition].restaurant[chilPosition].fav_progress=true
                                        mAdapter!!.notifyDataSetChanged()
                                        call_favorite = ApiCall.add_favorite_restaurant(jsonObject = postParam)
                                        remove_favorite_restaurant(call_favorite!!,list[parentPosition].restaurant[chilPosition])
                                    }
                                    override fun onNegativeButtonClick() {
                                    }
                                })

                            }else{
                                // favourite---
                                list[parentPosition].restaurant[chilPosition].fav_progress=true
                                mAdapter!!.notifyDataSetChanged()
                                call_favorite = ApiCall.add_favorite_restaurant(jsonObject = postParam)
                                setfavorite(call_favorite!!,list[parentPosition].restaurant[chilPosition])
                            }

                        }
                    }
                }
            })

            val sectionItemDecoration = RecyclerSectionItemDecoration(90,
                    true,
                    getSectionCallback(list))
          //  removeItemDecoration(sectionItemDecoration)
            while (recycler_view_parent.getItemDecorationCount() > 0) {
                loge(TAG, "clicked---"+recycler_view_parent.getItemDecorationCount() )
                recycler_view_parent.removeItemDecorationAt(0);
            }
            addItemDecoration(sectionItemDecoration)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }


        progress_bar.visibility = View.GONE


    }

    override fun comman_apisuccess(jsonObject: JsonObject, api_tag: String) {
        updatefavourite()
    }

    override fun comman_apifailed(error: String, api_tag: String) {
        updatefavourite()
    }


    private fun getSectionCallback(list: ArrayList<StatusWiseRestaurant>): RecyclerSectionItemDecoration.SectionCallback {
        return object : RecyclerSectionItemDecoration.SectionCallback {
           override fun isSection(position: Int): Boolean {
                return position == 0 || position == 1 || position == 2
            }

            override  fun getSectionHeader(position: Int): CharSequence {
                return list[position]
                        .status
            }
        }
    }


    private fun createViewModel(): UIModel =

            ViewModelProviders.of(this).get(RestaurantList.UIModel::class.java).apply {
                restaurantList.removeObservers(this@RestaurantList)
                restaurantList.observe(this@RestaurantList, Observer<RestaurantListModel> {
                    refreshview()
                })
            }


    class UIModel : ViewModel() {

        var restaurantList = MutableLiveData<RestaurantListModel>()
        var kokkenType_list = ArrayList<kokken_Model>()
        var tilpassort_list = ArrayList<kokken_Model>()
        var easysort_list = ArrayList<kokken_Model>()


    }

    override fun onDestroyView() {

        super.onDestroyView()

        logd(TAG, "onDestroyView...")

        ui_model?.let {
            ViewModelProviders.of(this).get(UIModel::class.java).restaurantList.removeObservers(this@RestaurantList)
        }

        call_restaurantlist?.let {
            progress_bar.visibility = View.GONE
            it.cancel()
        }

        if (call_favorite != null) {
            call_favorite!!.cancel()
        }

    }


    override fun onDetach() {
        super.onDetach()
        logd(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }

    override fun onStart() {
        super.onStart()
        logd(TAG, "onStart...")
        GlobalBus.bus.register(this)
    }

    override fun onStop() {
        super.onStop()
        logd(TAG, "onStop...")
        GlobalBus.bus.unregister(this)

    }

    fun onBackpress() {

        if(search_tool.visibility == View.VISIBLE){
            search_edt.text.clear()
            is_from_filter=false
            toolbar.visibility = View.VISIBLE
            search_tool.visibility = View.GONE
            search_edt.clearFocus()
            hideKeyboard()

        }else{
            if(txt_toolbar.text == getString(R.string.favourite_restauranter)){
                val parsingEvents =ParsingEvents.EventFromRestaurantList ()
                GlobalBus.bus.post(parsingEvents)
                ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(2,0)
            }else{
                ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().childFragmentManager.popBackStack()
            }
        }

    }

    data class kokken_Model(val itemtype: String, val itemcount: Int =0 , var is_itemselected: Boolean = false) : Serializable


    data class StatusWiseRestaurant(
            // status : order type like : pre order, new order
            // order type : Mainly use to show on button

            val status: String = "",
            val ordertype: String = "",
            val restaurant: ArrayList<Restaurant>
    ) : ModelUtility()


    class MyClickHandler(val restaurantlist: RestaurantList) {


        fun kokkenType(view: View) {

            if(restaurantlist.progress_bar.visibility == View.GONE  && restaurantlist.list.size > 0  || restaurantlist.is_from_filter){
                val intent = Intent(restaurantlist.context, KokkenType::class.java)
                val bundle = Bundle()
                if(restaurantlist.restaurantlistmodel !=null){
                    // move : actual list
                    restaurantlist.is_from_filter=true
                    bundle.putSerializable(Constants.KOKKEN_RESTAURANTLISTMODEL, restaurantlist.restaurantlistmodel)
                    intent.putExtra(Constants.BUNDLE, bundle)
                    restaurantlist.startActivityForResult(intent, Constants.REQ_FILTER_RESAURANT_LIST)
                }
            }
        }

        fun searchType(view: View) {

            if(restaurantlist.progress_bar.visibility == View.GONE && restaurantlist.list.size > 0 ){
                restaurantlist.app_bar.setExpanded(true)
                restaurantlist.search_edt.requestFocus()
                restaurantlist.showKeyboard()
                restaurantlist.toolbar.visibility = View.GONE
                restaurantlist.search_tool.visibility = View.VISIBLE

            }
        }

        fun tilpas(view: View) {

            if(restaurantlist.progress_bar.visibility == View.GONE && restaurantlist.list.size  > 0 || restaurantlist.is_from_filter ){
                val intent = Intent(restaurantlist.context, Tilpas::class.java)
                val bundle = Bundle()
                restaurantlist.is_from_filter=true
                bundle.putSerializable(Constants.TILPAS_RESTAURANTLISTMODEL, restaurantlist.restaurantlistmodel)
                Log.e("TAG","btn actual--"+restaurantlist.restaurantlistmodel!!.restaurant_list.open_now.size.toString())
                intent.putExtra(Constants.BUNDLE, bundle)
                restaurantlist.startActivityForResult(intent, Constants.REQ_SORT_RESAURANT_LIST)
            }
        }
    }


}
