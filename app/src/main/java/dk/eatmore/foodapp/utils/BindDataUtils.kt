package dk.eatmore.foodapp.utils

import android.databinding.BindingAdapter
import android.view.View

class BindDataUtils {

    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }
}