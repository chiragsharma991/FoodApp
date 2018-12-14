package dk.eatmore.foodapp.adapter.cart

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CalculateAttribute
import dk.eatmore.foodapp.databinding.RowChildCartViewBinding
import dk.eatmore.foodapp.model.cart.ProductAttributeValueItem
import dk.eatmore.foodapp.utils.BindDataUtils
import java.util.ArrayList

class CartChildViewAdapter(val context: Context, val listner: CartViewAdapter.AdapterListener, val parentPosition: Int, val list_child: ArrayList<ProductAttributeValueItem>, var p_id : String, var calculateAttribute: ArrayList<CalculateAttribute>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_ITEM = 1
    private var count: Int = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null
        if (viewType == VIEW_ITEM) {
            val binding: RowChildCartViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_child_cart_view, parent, false)
            binding.util=BindDataUtils
            vh = MyViewHolder(binding)
        }

        return vh!!
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // (holder as MyViewHolder).binding
        if (holder is MyViewHolder) {

            holder.binding.data=list_child[position]

            if (list_child[position].is_itemselected){
                holder.binding.itemRccTxt.setTextColor(ContextCompat.getColor(context, R.color.theme_color))
                holder.binding.priceRccTxt.setTextColor(ContextCompat.getColor(context, R.color.theme_color))
           //     val model=CalculateAttribute(p_id,list_child[position].pad_id,list_child[position].a_price)
                calculateAttribute.get(parentPosition).p_id = p_id
                calculateAttribute.get(parentPosition).pad_id = list_child[position].pad_id
                calculateAttribute.get(parentPosition).a_price = list_child[position].a_price
            }else{
                holder.binding.itemRccTxt.setTextColor(ContextCompat.getColor(context, R.color.black_txt_light))
                holder.binding.priceRccTxt.setTextColor(ContextCompat.getColor(context, R.color.black_txt_light))
            }

            holder.binding.rowChildCartItemlayout.setOnClickListener {
                listner.itemClicked(false,parentPosition,position)

            }


            /* holder.binding.rowChildCartAddBtn.setOnClickListener{
                 if(holder.binding.rowChildCartAdditional.visibility == View.VISIBLE){
                     count++
                     holder.binding.rowChildCartQtyTxt.text=count.toString()
                 }else{
                     listner.itemClicked(false,parentPosition,position)
                     holder.binding.rowChildCartAdditional.visibility=View.VISIBLE
                 }

             }
             holder.binding.rowChildCartRemoveBtn.setOnClickListener{
                 if(holder.binding.rowChildCartQtyTxt.text.equals("1")){
                     holder.binding.rowChildCartAdditional.visibility=View.GONE

                 }else{
                     count--
                     holder.binding.rowChildCartQtyTxt.text=count.toString()
                 }


             }*/

            Log.e("count", "" + count)
        }

    }


    private class MyViewHolder(val binding: RowChildCartViewBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun getItemCount(): Int {
        return list_child.size
    }


    override fun getItemViewType(position: Int): Int {
        return VIEW_ITEM
    }


}
