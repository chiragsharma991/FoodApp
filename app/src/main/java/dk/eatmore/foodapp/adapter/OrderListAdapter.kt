package dk.eatmore.foodapp.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.RowOrderPreferredBinding
import kotlinx.android.extensions.LayoutContainer

class OrderListAdapter(val context: Context, val callback : AdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val VIEW_ITEM = 1
    lateinit var listner : AdapterListener





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        var vh :RecyclerView.ViewHolder?=null
        if(viewType == VIEW_ITEM){
            this.listner=callback
           val binding :RowOrderPreferredBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.row_order_preferred,parent,false)
            vh = MyViewHolder(binding)
        }

        return vh!!
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            //val holder: MyViewHolder = holder
           // holder.init()
            holder.binding.rowOrderCardview.setOnClickListener {
                listner.itemClicked(position)
            }


        }

    }


    class MyViewHolder(val binding :RowOrderPreferredBinding) : RecyclerView.ViewHolder(binding.root)  {

    }







    override fun getItemCount(): Int {
        return 6
    }


    override fun getItemViewType(position: Int): Int {
        return VIEW_ITEM
    }


    interface AdapterListener {
        fun itemClicked(position : Int)
    }

}