package dk.eatmore.foodapp.adapter.extratoppings

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


class ExtratoppingsChildAdapter(val context: Context, val listner: ExtratoppingsAdapter.AdapterListener, val parentPosition: Int, val list_child: ArrayList<Topping_subgroup_details>, val calculateExtratoppings : ArrayList<CalculateExtratoppings>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_ITEM = 1
    private var count: Int = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null
        if (viewType == VIEW_ITEM) {
            val binding: RowChildExtratopViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_child_extratop_view, parent, false)
            vh = MyViewHolder(binding)
        }

        return vh!!
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // (holder as MyViewHolder).binding
        if (holder is MyViewHolder) {

            holder.binding.data=list_child[position]
            holder.binding.rowChildCartItemlayout.setOnClickListener {
                   when (holder.binding.checkboxRce.isChecked ){
                   true -> {
                       holder.binding.checkboxRce.isChecked =false
                       list_child[position].is_et_itemselected = false
                       //val calculateAttribute: ArrayList<ArrayList<CalculateAttribute>>
                    //   val model=CalculateAttribute(p_id,list_child[position].pad_id,list_child[position].a_price)
                     //  if(calculateExtratoppings ==null){
                           //val calculateExtratoppings : ArrayList<CalculateExtratoppings>
                       //    calculateExtratoppings = ArrayList<CalculateExtratoppings>()
                       //    val model =CalculateExtratoppings(list_child[position].tsgd_id)
                      //     calculateExtratoppings.add(model)

                     //  }else{

                       val model =CalculateExtratoppings(list_child[position].tsgd_id)
                       calculateExtratoppings.remove(model)


                 //      }
                   /*    val model=CalculateAttribute(calculateAttribute.get(0).get(0).p_id,calculateAttribute.get(0).get(0).pad_id,calculateAttribute.get(0).get(0).a_price,
                               (CalculateExtratoppings(calculateAttribute.get(0).get(0).p_id,calculateAttribute.get(0).get(0).pad_id,list_child[position].tsgd_id)))
                       calculateAttribute.get(0).set(0,model)*/
                   }
                   false ->{
                       holder.binding.checkboxRce.isChecked =true
                       list_child[position].is_et_itemselected = true
                       val model =CalculateExtratoppings(list_child[position].tsgd_id)
                       calculateExtratoppings.add(model)


                   }
               }
            }


        }

    }


    private class MyViewHolder(val binding: RowChildExtratopViewBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun getItemCount(): Int {
        return list_child.size
    }


    override fun getItemViewType(position: Int): Int {
        return VIEW_ITEM
    }


}