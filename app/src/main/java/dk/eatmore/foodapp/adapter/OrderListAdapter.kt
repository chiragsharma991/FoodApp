package dk.eatmore.foodapp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import kotlinx.android.extensions.LayoutContainer

class OrderListAdapter(context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val VIEW_ITEM = 1







    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        var vh :RecyclerView.ViewHolder?=null
        if(viewType == VIEW_ITEM){
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_order_preferred, parent, false)
            vh = MyViewHolder(itemView)
        }

        return vh!!
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            var holder: MyViewHolder = holder
            holder.init()


        }

    }


    class MyViewHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun init() {


        }

    }







    override fun getItemCount(): Int {
        return 6
    }


    override fun getItemViewType(position: Int): Int {
        return VIEW_ITEM
    }


}