package dk.eatmore.foodapp.adapter.onlyextratoppings


import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.RowChildCartViewBinding
import dk.eatmore.foodapp.databinding.RowChildExtratopViewBinding
import dk.eatmore.foodapp.model.cart.ProductAttributeValueItem
import dk.eatmore.foodapp.model.cart.Topping_subgroup_details
import java.util.ArrayList
import android.widget.CompoundButton
import dk.eatmore.foodapp.activity.main.cart.CalculateAttribute
import dk.eatmore.foodapp.activity.main.cart.CalculateExtratoppings
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.adapter.extratoppings.ExtratoppingsAdapter
import dk.eatmore.foodapp.databinding.RowOnlychildExtratopViewBinding
import dk.eatmore.foodapp.utils.BindDataUtils


class OnlyExtratoppingsChildAdapter(val context: Context, val listner: OnlyExtratoppingsAdapter.AdapterListener, val parentPosition: Int, val list_child: ArrayList<Topping_subgroup_details>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_ITEM = 1
    private var count: Int = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null
        if (viewType == VIEW_ITEM) {
            val binding: RowOnlychildExtratopViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_onlychild_extratop_view, parent, false)
            binding.util=BindDataUtils
            vh = MyViewHolder(binding)
        }

        return vh!!
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // (holder as MyViewHolder).binding
        if (holder is MyViewHolder) {

            holder.binding.data=list_child[position]
            holder.binding.rowOnlychildCartItemlayout.setOnClickListener {
                when (holder.binding.checkboxRoce.isChecked ){
                    true -> {
                        holder.binding.checkboxRoce.isChecked =false
                        list_child[position].is_et_itemselected = false
                    }
                    false ->{
                        holder.binding.checkboxRoce.isChecked =true
                        list_child[position].is_et_itemselected = true
                    }
                }

                CartActivity.ui_model!!.any_selection.value=true
            }


        }

    }


    private class MyViewHolder(val binding: RowOnlychildExtratopViewBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun getItemCount(): Int {
        return list_child.size
    }


    override fun getItemViewType(position: Int): Int {
        return VIEW_ITEM
    }


}