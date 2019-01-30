package dk.eatmore.foodapp.adapter.restaurantList


import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.RowRestaurantlistPBinding
import java.util.ArrayList
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList


class RestaurantListParentAdapter(val c: Context, val list : ArrayList<RestaurantList.StatusWiseRestaurant>, val callback: AdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    lateinit var listner : AdapterListener
    lateinit var status_keys: ArrayList<String>





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        var vh :RecyclerView.ViewHolder?=null
            this.listner=callback
            val binding :RowRestaurantlistPBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_restaurantlist_p,parent,false)
            vh = MyViewHolder(binding)


        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MyViewHolder) {
            holder.binding.status = list.get(position).status

            holder.binding.recyclerViewChild.apply {
                val mAdapter = RestaurantListChildAdapter(c, listner, position,list)
                layoutManager = LinearLayoutManager(c)
                adapter = mAdapter
            }
            holder.binding.executePendingBindings()
        }
    }


    private class MyViewHolder(val binding :RowRestaurantlistPBinding) : RecyclerView.ViewHolder(binding.root)  {

    }







    override fun getItemCount(): Int {
        return list.size
    }



    interface AdapterListener {
        fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int,tag : String)
    }




}