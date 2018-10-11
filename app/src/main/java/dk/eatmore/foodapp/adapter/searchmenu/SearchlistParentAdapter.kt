package dk.eatmore.foodapp.adapter.searchmenu

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.adapter.restaurantList.RestaurantListChildAdapter
import dk.eatmore.foodapp.databinding.RowRestaurantlistPBinding
import dk.eatmore.foodapp.databinding.RowSearchlistPBinding
import dk.eatmore.foodapp.fragment.ProductInfo.CategoryList
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.ProductListItem
import dk.eatmore.foodapp.utils.BindDataUtils
import java.util.ArrayList



class SearchlistParentAdapter(val c: Context, val list : ArrayList<MenuListItem>,var list_filtered : ArrayList<MenuListItem>, val callback: AdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(),Filterable{

    lateinit var listner : AdapterListener
    private lateinit var mAdapter: SearchlistChildAdapter


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh : RecyclerView.ViewHolder?=null
        this.listner=callback
        val binding : RowSearchlistPBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_searchlist_p,parent,false)
        vh = MyViewHolder(binding)
        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MyViewHolder) {

            holder.binding.menulistItem=list_filtered[position]
            holder.binding.recyclerViewChild.apply {
                 mAdapter = SearchlistChildAdapter(c, listner, position,list,list_filtered)
                mAdapter.setHasStableIds(true)
                layoutManager = LinearLayoutManager(c)
                adapter = mAdapter

            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class MyViewHolder(val binding : RowSearchlistPBinding) : RecyclerView.ViewHolder(binding.root)  {

    }

    override fun getItemCount(): Int {
        return list_filtered.size

    }

    override fun getFilter(): Filter {

        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence): Filter.FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    list_filtered = arrayListOf()
                } else {
                    val filteredList = ArrayList<MenuListItem>()
                    for (i in 0 until list.size) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        val arrayList = ArrayList<ProductListItem>()
                        for (j in 0 until list.get(i).product_list!!.size){
                            if(list.get(i).product_list!![j].p_name.toLowerCase().contains(charString.toLowerCase())){
                                arrayList.add(ProductListItem("","", arrayListOf(),list.get(i).product_list!![j].p_desc,"","",list.get(i).product_list!![j].p_name,"","","","",""))
                                filteredList.add(MenuListItem("","","","",list.get(i).c_name,arrayList))
                            }
                        }
                    }

                    list_filtered = filteredList
                    Log.e("check before--- : ",""+list_filtered.size)
                }

                val filterResults = Filter.FilterResults()
                filterResults.values = list_filtered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
                list_filtered = filterResults.values as ArrayList<MenuListItem>
                Log.e("check after--- : ",""+list_filtered.size)
                notifyDataSetChanged()
            }
        }
    }


    interface AdapterListener {
        fun itemClicked(parentView : Boolean , parentPosition : Int, chilPosition : Int)
    }




}