package dk.eatmore.foodapp.adapter.gift

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.Coupan
import java.util.ArrayList
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.Profile
import dk.eatmore.foodapp.databinding.RawGiftbalancePBinding
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants


class GiftBalanceParentAdapter(val myclickhandler : Coupan.MyClickHandler, val c: Context, val list : ArrayList<Profile.GiftType>, val callback: AdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    lateinit var listner : AdapterListener
    lateinit var status_keys: ArrayList<String>





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        var vh :RecyclerView.ViewHolder?=null
        this.listner = callback
        val binding :RawGiftbalancePBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.raw_giftbalance_p,parent,false)
        vh = MyViewHolder(binding)
        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is MyViewHolder) {

            val builder = SpannableStringBuilder()
            val span1 = SpannableString(if(list[position].giftType == Constants.EATMORE) "EatMore : " else "Restauranter : ")
            val span2 = SpannableString(if(list[position].giftType == Constants.EATMORE) "kr. "+BindDataUtils.convertCurrencyToDanishWithoutLabel(list[position].giftTotal.trim()) else "")
            span2.setSpan(ForegroundColorSpan(ContextCompat.getColor(c, R.color.theme_color)), 0, span2.trim().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(span1).append(span2)

            //holder.binding.giftType=list[position].giftType
            holder.binding.rowCartTxt.text=builder
            holder.binding.recyclerViewChild.apply {
                val mAdapter = GiftBalanceChildAdapter(myclickhandler,c, listner, position,list)
                layoutManager = LinearLayoutManager(c)
                adapter = mAdapter
            }
            holder.binding.executePendingBindings()
        }
    }


    private class MyViewHolder(val binding :RawGiftbalancePBinding) : RecyclerView.ViewHolder(binding.root)  {

    }

    override fun getItemCount(): Int {
        return list.size
    }



    interface AdapterListener {
        fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int)
    }




}