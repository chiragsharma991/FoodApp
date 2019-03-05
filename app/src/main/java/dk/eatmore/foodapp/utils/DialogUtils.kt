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
      /*  val textView = alert.findViewById<TextView>(android.R.id.message) as TextView
        val face = Typeface.createFromAsset(context.getAssets(), "fonts/FONT")
        textView.typeface = face*/
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
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color)
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color)


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







    interface OnDialogClickListener {
        fun onPositiveButtonClick(position : Int)
        fun onNegativeButtonClick()
    }
}
