package dk.eatmore.foodapp.adapter.onlyextratoppings

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CalculateExtratoppings
import dk.eatmore.foodapp.adapter.extratoppings.ExtratoppingsChildAdapter
import dk.eatmore.foodapp.databinding.RowExtratoppingsViewBinding
import dk.eatmore.foodapp.databinding.RowOnlyextratoppingsViewBinding
import dk.eatmore.foodapp.model.cart.Topping_subgroup_list

class OnlyExtratoppingsAdapter(val c: Context, val list :ArrayList<Topping_subgroup_list>, val callback: AdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val VIEW_ITEM = 1
    lateinit var listner : AdapterListener




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh : RecyclerView.ViewHolder?=null
        if(viewType == VIEW_ITEM){
            this.listner=callback
            val binding : RowOnlyextratoppingsViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_onlyextratoppings_view,parent,false)
            vh = MyViewHolder(binding)
        }

        return vh!!
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            //val holder: MyViewHolder = holder
            holder.binding.data= list[position]

            /* holder.binding.rowOrderCardview.setOnClickListener {
                 listner.itemClicked(position)
             }
 */
            //  holder.binding.parentChild.visibility= View.GONE
            /*      holder.binding.rowCartTxt.setOnClickListener{
                      if(holder.binding.parentChild.visibility == View.VISIBLE)
                          holder.binding.parentChild.visibility=View.GONE
                      else
                          holder.binding.parentChild.visibility=View.VISIBLE
                  }*/
            holder.binding.rowOnlyextratopChildRecyclerView.apply {
                val mAdapter = OnlyExtratoppingsChildAdapter(c, listner, position, list[position].topping_subgroup_details)
                layoutManager = LinearLayoutManager(c)
                adapter = mAdapter
            }
            holder.binding.executePendingBindings()



        }

    }


    private class MyViewHolder(val binding : RowOnlyextratoppingsViewBinding) : RecyclerView.ViewHolder(binding.root)  {

    }







    override fun getItemCount(): Int {
        return list.size
    }


    override fun getItemViewType(position: Int): Int {
        return VIEW_ITEM
    }


    interface AdapterListener {
        fun itemClicked(parentView : Boolean , parentPosition : Int, chilPosition : Int)
    }




}
