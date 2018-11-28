package dk.eatmore.foodapp.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.R.string.dismiss
import android.R.drawable.btn_dialog
import android.databinding.adapters.TextViewBindingAdapter.setText
import android.widget.TextView
import android.view.Window.FEATURE_NO_TITLE
import android.app.Activity
import android.app.Dialog
import android.graphics.Typeface
import android.support.v7.widget.AppCompatTextView
import android.view.Gravity
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import dk.eatmore.foodapp.model.home.Restaurant
import kotlinx.android.synthetic.main.infodialog.*


object DialogUtils {

    fun openDialog(context: Context, msg: String,title : String, btnPositive : String, btnNegative: String, color:Int, onDialogClickListener: OnDialogClickListener) {
        val builder = AlertDialog.Builder(context, R.style.AppCompatAlertDialogDefaultStyle)
        builder.setMessage(msg)
        builder.setTitle(title)
        builder.setCancelable(false)
        builder.setPositiveButton(btnPositive) { _, _ ->
            onDialogClickListener.onPositiveButtonClick(0)
            Log.e("onPositiveButtonClick","---")
        }
        /**
         * If blank then dont show negative button
         */
        if(btnNegative != "") {
            builder.setNegativeButton(btnNegative, { _, _ ->
                onDialogClickListener.onNegativeButtonClick()
                Log.e("setNegativeButton","---")

            })
        }
        val alert = builder.create()
        alert.show()

        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color)
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color)
    }
    fun openDialogDefault(context: Context, msg: String,title : String, btnPositive : String, btnNegative: String, color:Int, onDialogClickListener: OnDialogClickListener) {
        val builder = AlertDialog.Builder(context, R.style.AppCompatAlertDialogDefaultStyle)
        builder.setMessage(msg)
        builder.setTitle(title)
        builder.setCancelable(false)
        builder.setPositiveButton(btnPositive) { _, _ ->
            onDialogClickListener.onPositiveButtonClick(0)
            Log.e("onPositiveButtonClick","---")
        }
        /**
         * If blank then dont show negative button
         */
        if(btnNegative != "") {
            builder.setNegativeButton(btnNegative, { _, _ ->
                onDialogClickListener.onNegativeButtonClick()
                Log.e("setNegativeButton","---")

            })
        }
        val alert = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context,R.color.black_light))
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context,R.color.black_light))


    }


    fun createDialog(context: Context, view : View) : AlertDialog {
        val builder = AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle_Transparent)
        builder.setView(view)
        var dialog = builder.create()
        dialog.setCancelable(true)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    fun createListDialog( title : String ,activity: FragmentActivity?, list : ArrayList<String>,borderColor : Int, onDialogClickListener: OnDialogClickListener) {

        val builderSingle = AlertDialog.Builder(activity!!)
        //builderSingle.setIcon(R.drawable.ic_block)
        //builderSingle.setTitle("Select One Reason:")
        val inflater = activity.getLayoutInflater()
        val view = inflater.inflate( R.layout.row_alert_header, null)
       // val titleTxt = view.select_alert_header as TextView
      //  val border = view.select_alert_header_view as LinearLayout
       // border.setBackgroundColor(borderColor)


      //  titleTxt.text = title
        builderSingle.setCustomTitle(view)
       // builderSingle.setMessage("testing")

        val arrayAdapter = ArrayAdapter<String>(activity, R.layout.simple_expandable_list_item_1
        )
        for (i in 0..list.size-1){
            arrayAdapter.add(list.get(i))
        }

        builderSingle.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builderSingle.setAdapter(arrayAdapter) { dialog, position ->

            dialog.dismiss ()
            onDialogClickListener.onPositiveButtonClick(position)

           /* val strName = arrayAdapter.getItem(position)
            val builderInner = AlertDialog.Builder(activity!!)
            builderInner.setMessage(strName)
            builderInner.setTitle("Selected reason is:")
            builderInner.setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss ()
                onDialogClickListener.onPositiveButtonClick(position)
            }
            builderInner.show()*/
        }
        builderSingle.show()

    }

    fun showDialog(restaurant: Restaurant,context: Context) {
        val dialog = Dialog(context,R.style.AppCompatAlertDialogStyle_Transparent)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.infodialog)

        val shippinginfo_container = dialog.shippinginfo_container as LinearLayout

        try {

            if (!(restaurant.shipping_charges.size > 0)) {
                // show empty
                return
            }
            shippinginfo_container.removeAllViewsInLayout()
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

                shippinginfo_container.addView(parent)
                val view= View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin=8
                view.alpha=0.3f
                view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                view.layoutParams=vparms
                shippinginfo_container.addView(view)


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
                    shippinginfo_container.addView(parent)
                    val view= View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin=8
                    view.alpha=0.3f
                    view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                    view.layoutParams=vparms
                    shippinginfo_container.addView(view)



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

                shippinginfo_container.addView(parent)
                val view= View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin=8
                view.alpha=0.3f
                view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                view.layoutParams=vparms
                shippinginfo_container.addView(view)


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
                    shippinginfo_container.addView(parent)
                    val view= View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin=8
                    view.alpha=0.3f
                    view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                    view.layoutParams=vparms
                    shippinginfo_container.addView(view)



                }


            } else if (restaurant.shipping_type == "by_order_price") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Fra (Pris)", "Til (Pris.)", "Pris (kr.)")
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

                shippinginfo_container.addView(parent)
                val view= View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin=8
                view.alpha=0.3f
                view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                view.layoutParams=vparms
                shippinginfo_container.addView(view)


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
                    shippinginfo_container.addView(parent)
                    val view= View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin=8
                    view.alpha=0.3f
                    view.background=ContextCompat.getDrawable(context!!,R.color.divider_color)
                    view.layoutParams=vparms
                    shippinginfo_container.addView(view)



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

                shippinginfo_container.addView(parent)


            }


        } catch (e: Exception) {
            Log.e("exception",e.message.toString())
        }

            dialog.show()

    }






    interface OnDialogClickListener {
        fun onPositiveButtonClick(position : Int)
        fun onNegativeButtonClick()
    }
}
