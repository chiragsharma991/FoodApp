package dk.eatmore.foodapp.adapter.searchmenu

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.RowSearchlistPBinding
import dk.eatmore.foodapp.model.home.MenuListItem
import java.util.*


class SearchlistParentAdapter(val c: Context, val list : ArrayList<MenuListItem>,var list_filtered : ArrayList<MenuListItem>, val callback: AdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    lateinit var listner : AdapterListener
    private lateinit var mAdapter: SearchlistChildAdapter
    private var searchString : String =""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh : RecyclerView.ViewHolder?=null
        this.listner=callback
        val binding : RowSearchlistPBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_searchlist_p,parent,false)
        vh = MyViewHolder(binding)
        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MyViewHolder) {
            Log.e("TAG,","on bind---")
         //   holder.binding.menulistItem=list_filtered[position]
            holder.binding.rowCartTxt.text=list_filtered[position].c_name
            holder.binding.recyclerViewChild.apply {
                 mAdapter = SearchlistChildAdapter(c, listner, position,list,list_filtered)
                mAdapter.setHasStableIds(true)
                layoutManager = LinearLayoutManager(c)
                adapter = mAdapter

            }
            holder.binding.executePendingBindings()
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

     fun refreshlist(menu_list_filtered: ArrayList<MenuListItem>) {
         list_filtered=menu_list_filtered
        notifyDataSetChanged()
    }

    interface AdapterListener {
        fun itemClicked(parentView : Boolean , parentPosition : Int, chilPosition : Int)
    }




}