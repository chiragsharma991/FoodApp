package dk.eatmore.foodapp.adapter.restaurantList

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.databinding.RowRestaurantlistCBinding
import java.util.ArrayList


class RestaurantListChildAdapter(val context: Context, val listner: RestaurantListParentAdapter.AdapterListener, val parentPosition: Int, val list : ArrayList<RestaurantList.StatusWiseRestaurant> ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var count: Int = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null
            val binding: RowRestaurantlistCBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_restaurantlist_c, parent, false)
           // binding.util= BindDataUtils
            vh = MyViewHolder(binding)


        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            Glide.with(context).load(list.get(parentPosition).restaurant.get(position).app_icon).into(holder.binding.imageview);
          //  ViewCompat.setTransitionName(holder.binding.imageview, position.toString() + "vnhvn")

            holder.binding.restaurant=list.get(parentPosition).restaurant.get(position)
            holder.binding.ordertype=list.get(parentPosition).ordertype
            holder.binding.rowChildItem.setOnClickListener {
                listner.itemClicked(false,parentPosition,position)

            }
        }
    }


    private class MyViewHolder(val binding: RowRestaurantlistCBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun getItemCount(): Int {
        return list.get(parentPosition).restaurant.size
    }




}
