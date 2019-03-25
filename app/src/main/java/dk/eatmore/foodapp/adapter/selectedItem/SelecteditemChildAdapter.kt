package dk.eatmore.foodapp.adapter.selectedItem

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.bumptech.glide.Glide
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CalculateAttribute
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.SearchMenu
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.adapter.restaurantList.RestaurantListParentAdapter
import dk.eatmore.foodapp.databinding.RowChildCartViewBinding
import dk.eatmore.foodapp.databinding.RowRestaurantlistCBinding
import dk.eatmore.foodapp.databinding.RowSearchlistCBinding
import dk.eatmore.foodapp.databinding.RowSelectedItemlistCBinding
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.cart.ProductAttributeValueItem
import dk.eatmore.foodapp.model.epay.ResultItem
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.ProductListItem
import dk.eatmore.foodapp.utils.BindDataUtils
import java.util.*


class SelecteditemChildAdapter (val context: Context, val listner: SelecteditemParentAdapter.AdapterListener, val parentPosition: Int, val list: ArrayList<ResultItem> ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null
        val binding: RowSelectedItemlistCBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_selected_itemlist_c, parent, false)
        // binding.util= BindDataUtils
        vh = MyViewHolder(binding)
        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            //  holder.binding.util=BindDataUtils

            holder.binding.executePendingBindings()
        }
    }


    private class MyViewHolder(val binding: RowSelectedItemlistCBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun getItemCount(): Int {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }




}

