package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.HealthReport
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.InfoRestaurantBinding
import dk.eatmore.foodapp.databinding.RowOpeningHoursBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.home.Opening_hours
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.info_restaurant.*
import dk.eatmore.foodapp.R.id.linearLayout
import android.databinding.adapters.ViewBindingAdapter.setPadding
import android.databinding.adapters.TextViewBindingAdapter.setText
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import dk.eatmore.foodapp.utils.BindDataUtils


class Info : BaseFragment() {

    private lateinit var binding: InfoRestaurantBinding
    private lateinit var mAdapter: UniversalAdapter<Opening_hours, RowOpeningHoursBinding>
    private lateinit var restaurant: Restaurant


    companion object {

        val TAG = "Info"
        fun newInstance(restaurant: Restaurant): Info {
            val fragment = Info()
            val bundle = Bundle()
            bundle.putSerializable(Constants.RESTAURANT, restaurant)
            fragment.arguments = bundle
            return fragment
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
            val myclickhandler = MyClickHandler(this)
            restaurant = arguments!!.getSerializable(Constants.RESTAURANT) as Restaurant
            binding.restaurant = restaurant
            binding.handler = myclickhandler
            refresh_view()

        } else {
            logd(TAG, "saveInstance NOT NULL")

        }
    }


    private fun refresh_view() {

        mAdapter = UniversalAdapter(context!!, restaurant.opening_hours, R.layout.row_opening_hours, object : RecyclerCallback<RowOpeningHoursBinding, Opening_hours> {
            override fun bindData(binder: RowOpeningHoursBinding, model: Opening_hours) {
                setRecyclerData(binder, model)
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view.adapter = mAdapter

        addDeliveryinformation()

    }


    private fun setRecyclerData(binder: RowOpeningHoursBinding, model: Opening_hours) {
        binder.openingHours = model
    }


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

    class MyClickHandler(val info: Info) {


        fun tapOnHealthReport(view: View) {
            info.tapOnHealthReport()
        }


    }

    private fun tapOnHealthReport() {

        val fragment = HealthReport.newInstance()
        val homefragment: HomeFragment = ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment()
        homefragment.addFragment(R.id.home_fragment_container, fragment, HealthReport.TAG, true)


    }

    private fun addDeliveryinformation() {

        try {

            if (!(restaurant.shipping_charges.size > 0)) {
                // show empty
                return
            }
            linearcontainer.removeAllViewsInLayout()
            if (restaurant.shipping_type == "by_distance") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Fra (km)", "Til (km)", "Pris (kr.)")
                // Add header
                for (i in 0..2) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 8
                    else if (i == 2)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms


                    textView1.text = headerlist[i]
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.START
                    else if (i == 2)
                        textView1.gravity = Gravity.START
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.TextViewSmall)
                    textView1.typeface= Typeface.DEFAULT_BOLD
                    textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                    parent.addView(textView1)

                }

                linearcontainer.addView(parent)
                val view= View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin=8
                view.alpha=0.3f
                view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                view.layoutParams=vparms
                linearcontainer.addView(view)


                // Add values
                for (i in 0 until restaurant.shipping_charges.size) {
                    parent = LinearLayout(context)
                    val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.topMargin = 8
                    parent.layoutParams = parms
                    parent.orientation = LinearLayout.HORIZONTAL

                    // add row
                    for (j in 0..2) {
                        // add column
                        // Add textview 1
                        val textView1 = AppCompatTextView(context)
                        val parms = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        parms.weight = 1f
                        if (j == 0)
                            parms.rightMargin = 8
                        else if (j == 1)
                            parms.rightMargin = 8
                        else if (j == 2)
                            parms.rightMargin = 0
                        textView1.layoutParams = parms

                        if (j == 0)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].from_pd)
                        else if (j == 1)
                            textView1.text = if(restaurant.shipping_charges[i].to_pd ==null ) Constants.OPEFTER else BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].to_pd!!)
                        else if (j == 2)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].price)

                        if (j == 0)
                            textView1.gravity = Gravity.START
                        else if (j == 1)
                            textView1.gravity = Gravity.START
                        else if (j == 2)
                            textView1.gravity = Gravity.START
                        textView1.setSingleLine(true)
                        textView1.setTextAppearance(context, R.style.TextViewSmall)
                        textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                        parent.addView(textView1)

                    }
                    linearcontainer.addView(parent)
                    val view= View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin=8
                    view.alpha=0.3f
                    view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                    view.layoutParams=vparms
                    linearcontainer.addView(view)



                }


            } else if (restaurant.shipping_type == "by_postal") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Postnr.", "Min. (kr.)", "Pris (kr.)")
                // Add header
                for (i in 0..2) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 8
                    else if (i == 2)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms


                    textView1.text = headerlist[i]
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.START
                    else if (i == 2)
                        textView1.gravity = Gravity.START
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.TextViewSmall)
                    textView1.typeface= Typeface.DEFAULT_BOLD
                    textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                    parent.addView(textView1)

                }

                linearcontainer.addView(parent)
                val view= View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin=8
                view.alpha=0.3f
                view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                view.layoutParams=vparms
                linearcontainer.addView(view)


                // Add values
                for (i in 0 until restaurant.shipping_charges.size) {
                    parent = LinearLayout(context)
                    var parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.topMargin = 8
                    parent.layoutParams = parms
                    parent.orientation = LinearLayout.HORIZONTAL

                    // add row
                    for (j in 0..2) {
                        // add column
                        // Add textview 1
                        val textView1 = AppCompatTextView(context)
                        parms = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        parms.weight = 1f
                        if (j == 0)
                            parms.rightMargin = 8
                        else if (j == 1)
                            parms.rightMargin = 8
                        else if (j == 2)
                            parms.rightMargin = 0
                        textView1.layoutParams = parms

                        if (j == 0)
                            textView1.text = restaurant.shipping_charges[i].postal_code
                        else if (j == 1)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].minimum_order_price)
                        else if (j == 2)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].price)

                        if (j == 0)
                            textView1.gravity = Gravity.START
                        else if (j == 1)
                            textView1.gravity = Gravity.START
                        else if (j == 2)
                            textView1.gravity = Gravity.START
                        textView1.setSingleLine(true)
                        textView1.setTextAppearance(context, R.style.TextViewSmall)
                        textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                        parent.addView(textView1)

                    }
                    linearcontainer.addView(parent)
                    val view= View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin=8
                    view.alpha=0.3f
                    view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                    view.layoutParams=vparms
                    linearcontainer.addView(view)



                }


            } else if (restaurant.shipping_type == "by_order_price") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Fra (Pris)", "Til (Pris)", "Pris (kr.)")
                // Add header
                for (i in 0..2) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 8
                    else if (i == 2)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms


                    textView1.text = headerlist[i]
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.START
                    else if (i == 2)
                        textView1.gravity = Gravity.START
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.TextViewSmall)
                    textView1.typeface= Typeface.DEFAULT_BOLD
                    textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                    parent.addView(textView1)

                }

                linearcontainer.addView(parent)
                val view= View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin=8
                view.alpha=0.3f
                view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                view.layoutParams=vparms
                linearcontainer.addView(view)


                // Add values
                for (i in 0 until restaurant.shipping_charges.size) {
                    parent = LinearLayout(context)
                    var parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.topMargin = 8
                    parent.layoutParams = parms
                    parent.orientation = LinearLayout.HORIZONTAL

                    // add row
                    for (j in 0..2) {
                        // add column
                        // Add textview 1
                        val textView1 = AppCompatTextView(context)
                        parms = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        parms.weight = 1f
                        if (j == 0)
                            parms.rightMargin = 8
                        else if (j == 1)
                            parms.rightMargin = 8
                        else if (j == 2)
                            parms.rightMargin = 0
                        textView1.layoutParams = parms

                        if (j == 0)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].from_pd)
                        else if (j == 1)
                            textView1.text = if(restaurant.shipping_charges[i].to_pd ==null) Constants.OPEFTER else BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].to_pd!!)
                        else if (j == 2)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].price)

                        if (j == 0)
                            textView1.gravity = Gravity.START
                        else if (j == 1)
                            textView1.gravity = Gravity.START
                        else if (j == 2)
                            textView1.gravity = Gravity.START
                        textView1.setSingleLine(true)
                        textView1.setTextAppearance(context, R.style.TextViewSmall)
                        textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                        parent.addView(textView1)

                    }
                    linearcontainer.addView(parent)
                    val view= View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin=8
                    view.alpha=0.3f
                    view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                    view.layoutParams=vparms
                    linearcontainer.addView(view)



                }


            } else if (restaurant.shipping_type == "flat_rate") {

                val parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL
                val headerlist = arrayListOf("Pris (Kr.)", BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges.get(0).price))
                // Add header
                for (i in 0..1) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms
                    textView1.text = headerlist[i]
                    textView1.typeface= Typeface.DEFAULT_BOLD
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.START
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.TextViewSmall)
                    textView1.typeface= Typeface.DEFAULT_BOLD
                    textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB
                    parent.addView(textView1)
                }

                linearcontainer.addView(parent)



            }


        } catch (e: Exception) {
            loge(TAG, "dynamic text error:--- " + e.message)

        }


    }


}



