package dk.eatmore.foodapp.adapter.selectedItem

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.RowSearchlistPBinding
import dk.eatmore.foodapp.databinding.RowSelectedItemlistPBinding
import dk.eatmore.foodapp.model.epay.ResultItem
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.utils.BindDataUtils
import java.util.*


class SelecteditemParentAdapter (val c: Context, val list : ArrayList<ResultItem>, val callback: AdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    lateinit var listner : AdapterListener
    private lateinit var mAdapter: SelecteditemChildAdapter


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh : RecyclerView.ViewHolder?=null
        this.listner=callback
        val binding : RowSelectedItemlistPBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_selected_itemlist_p,parent,false)
        vh = MyViewHolder(binding)
        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MyViewHolder) {
            Log.e("TAG,","on bind---")

       /*     view.remove_item.visibility=View.GONE
            view.item_name.text = String.format(getString(R.string.qty_n_price),EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].quantity,EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].product_name)
            view.item_price.text=if(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].p_price !=null) BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].p_price!!) else "null"
            view.add_subitem_view.removeAllViewsInLayout()*/


            holder.binding.itemName.text=String.format(c.getString(R.string.qty_n_price),list.get(position).quantity,list.get(position).product_name)
            holder.binding.itemPrice.text=if(list.get(position).p_price !=null) BindDataUtils.convertCurrencyToDanishWithoutLabel(list.get(position).p_price!!) else "null"
            holder.binding.recyclerView.apply {
                mAdapter = SelecteditemChildAdapter(c, listner, position,list)
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

    private class MyViewHolder(val binding : RowSelectedItemlistPBinding) : RecyclerView.ViewHolder(binding.root)  {

    }

    override fun getItemCount(): Int {
        return list.size

    }

    fun refreshlist(menu_list_filtered: ArrayList<MenuListItem>) {
        //list_filtered=menu_list_filtered
       // notifyDataSetChanged()
    }

    interface AdapterListener {
        fun itemClicked(parentView : Boolean , parentPosition : Int, chilPosition : Int)
    }




}