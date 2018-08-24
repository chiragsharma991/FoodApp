package dk.eatmore.foodapp.utils

import android.databinding.BindingAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.model.cart.ProductAttributeValueItem

object BindDataUtils {

    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }


    fun ttt( data: ProductAttributeValueItem): String {
        if (data.is_itemselected)
            return "1"
          //  return ContextCompat.getColor(text.context, R.color.theme_color_secondary_dark)
        else
          //  return ContextCompat.getColor(text.context, R.color.black_light)
            return "3"

    }


    /*   fun convertToSuffix(count: Long): String {
           if (count < 1000) return "" + count
           val exp = (Math.log(count.toDouble()) / Math.log(1000.0)).toInt()
           return String.format("%.1f%c",
                   count / Math.pow(1000.0, exp.toDouble()),
                   "kmgtpe"[exp - 1])
       }*/


}