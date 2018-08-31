package dk.eatmore.foodapp.utils

import android.databinding.BindingAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.model.cart.ProductAttributeValueItem
import dk.eatmore.foodapp.model.home.ProductListItem
import java.text.NumberFormat
import java.util.*

object BindDataUtils {

/*
    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }
*/

    fun convertCurrencyToDanish(amount: String): String? {
        val deviceLocale = Locale.getDefault().language //  if (deviceLocale.equalsIgnoreCase("en")) {
        //      return formatValueToMoney(amount);
        //  } else {
        val mNumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
        var convertedAmount: String? = mNumberFormat.format(amount.toDouble())
        println(convertedAmount)
        if (convertedAmount != null && convertedAmount.length > 2) {
            convertedAmount = convertedAmount.substring(0, convertedAmount.length - 2)
        }
        return convertedAmount+"kr"
        //  }
    }
}

