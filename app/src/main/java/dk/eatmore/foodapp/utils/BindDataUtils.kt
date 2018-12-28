package dk.eatmore.foodapp.utils

import android.databinding.BindingAdapter
import android.net.ParseException
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.model.cart.ProductAttributeValueItem
import dk.eatmore.foodapp.model.home.ProductListItem
import java.text.NumberFormat
import java.util.*
import android.view.ViewGroup
import java.text.SimpleDateFormat


object BindDataUtils {

/*
    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }
*/



    fun convertCurrencyToDanish(amount: String): String? {

        try{
            val deviceLocale = Locale.getDefault().language //  if (deviceLocale.equalsIgnoreCase("en")) {
            //      return formatValueToMoney(amount);
            //  } else {
            val mNumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
            var convertedAmount: String? = mNumberFormat.format(amount.toDouble())
            println(convertedAmount)
            if (convertedAmount != null && convertedAmount.length > 2) {
                convertedAmount = convertedAmount.substring(0, convertedAmount.length - 2)
            }
            return convertedAmount+" kr."

        }catch (e : Exception){
            Log.e("BindDataUtils","Exception error---"+e.message)
            return "null"
        }

        //  }
    }
    fun convertCurrencyToDanishWithoutLabel(amount: String): String? {

        try{
            val deviceLocale = Locale.getDefault().language //  if (deviceLocale.equalsIgnoreCase("en")) {
            //      return formatValueToMoney(amount);
            //  } else {
            val mNumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
            var convertedAmount: String? = mNumberFormat.format(amount.toDouble())
            println(convertedAmount)
            if (convertedAmount != null && convertedAmount.length > 2) {
                convertedAmount = convertedAmount.substring(0, convertedAmount.length - 2)
            }
            return convertedAmount
            //  }

        }catch (e : Exception){
            Log.e("BindDataUtils","Exception error---"+e.message)
            return "null"
        }

    }


    fun calculateRatingline() : Float {
      return 70f

    }

    fun parseDateToddMMyyyy(time:String):String {
        val inputPattern = "yyyy-MM-dd HH:mm:ss"
        val outputPattern = "dd-MMM-yyyy HH:mm"
     //   val outputPattern = "dd-MMM-yyyy h:mm a"
        val inputFormat = SimpleDateFormat(inputPattern)
        val outputFormat = SimpleDateFormat(outputPattern)
        var date:Date? = null
        var str:String? = null
        try
        {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
        }
        catch (e: ParseException) {
            e.printStackTrace()
        }
        return str!!
    }


}

