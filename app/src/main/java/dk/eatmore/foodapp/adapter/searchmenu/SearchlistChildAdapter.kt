package dk.eatmore.foodapp.adapter.searchmenu


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
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.cart.ProductAttributeValueItem
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.ProductListItem
import dk.eatmore.foodapp.utils.BindDataUtils
import java.util.*


class SearchlistChildAdapter(val context: Context, val listner: SearchlistParentAdapter.AdapterListener, val parentPosition: Int, val list: ArrayList<MenuListItem>, var list_filtered: ArrayList<MenuListItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null
        val binding: RowSearchlistCBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_searchlist_c, parent, false)
        // binding.util= BindDataUtils
        vh = MyViewHolder(binding)
        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            //  holder.binding.util=BindDataUtils
            holder.binding.isRestaurantClosed=DetailsFragment.is_restaurant_closed
            holder.binding.productlistItem= list_filtered[parentPosition].product_list!![position]
            holder.binding.catTitle.text = list_filtered[parentPosition].product_list!![position].p_name
            holder.binding.catDesc.text = list_filtered[parentPosition].product_list!![position].p_desc
            val size=if(list_filtered[parentPosition].product_list!![position].product_attribute==null ) 0 else list_filtered[parentPosition].product_list!![position].product_attribute.size
            val fra_label = if(list_filtered[parentPosition].product_list!![position].is_attributes == "0" && list_filtered[parentPosition].product_list!![position].extra_topping_group == null ) "" else "fra: "
            holder.binding.catPrice.text = if(size > 0)  fra_label+getprice(list_filtered[parentPosition].product_list!![position]) else fra_label+BindDataUtils.convertCurrencyToDanish(list_filtered[parentPosition].product_list!![position].p_price ?: "0")


            // Find charText in wp
            val p_name = list_filtered[parentPosition].product_list!![position].p_name.toLowerCase()
            val p_desc = list_filtered[parentPosition].product_list!![position].p_desc.toLowerCase()
                 if (p_name.contains(SearchMenu.searchString)) {
                     val startPos = p_name.indexOf(SearchMenu.searchString)
                     val endPos = startPos + SearchMenu.searchString.length

                     val spanText = Spannable.Factory.getInstance().newSpannable(holder.binding.catTitle.text) // <- EDITED: Use the original string, as `country` has been converted to lowercase.
                     spanText.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.theme_color)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                     holder.binding.catTitle.text=spanText
                 }
                 if (p_desc.contains(SearchMenu.searchString)) {
                     val startPos = p_desc.indexOf(SearchMenu.searchString)
                     val endPos = startPos + SearchMenu.searchString.length

                     val spanText = Spannable.Factory.getInstance().newSpannable(holder.binding.catDesc.text) // <- EDITED: Use the original string, as `country` has been converted to lowercase.
                     spanText.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.theme_color)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                     holder.binding.catDesc.text=spanText
                 }

            holder.binding.rowChildItem.setOnClickListener {
                listner.itemClicked(false, parentPosition, position)
            }
            holder.binding.executePendingBindings()
        }
    }


    private class MyViewHolder(val binding: RowSearchlistCBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun getItemCount(): Int {
        return list_filtered[parentPosition].product_list?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    var attribute_cost: Double = 0.0
    fun getprice(productListItem: ProductListItem): String {
        attribute_cost = 0.0
        for (i in 0..productListItem.product_attribute.size - 1) {
            attribute_cost = attribute_cost + productListItem.product_attribute.get(i).default_attribute_value.a_price.toDouble()
        }
        return BindDataUtils.convertCurrencyToDanish(attribute_cost.toString()) ?: "null"
    }


}

