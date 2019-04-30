package dk.eatmore.foodapp.adapter.gift

import android.content.Context
import android.databinding.DataBindingUtil
import android.net.ParseException
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.Coupan
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.Profile
import dk.eatmore.foodapp.databinding.RawGiftbalanceCBinding
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.ImageLoader
import java.text.SimpleDateFormat
import java.util.*


class  GiftBalanceChildAdapter (val myclickhandler : Coupan.MyClickHandler, val context: Context, val listner: GiftBalanceParentAdapter.AdapterListener, val parentPosition: Int, val list: ArrayList<Profile.GiftType>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_EATMORE = 0
    private val TYPE_RESTAURANT = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null

        if(viewType == TYPE_EATMORE){
            val binding: RawGiftbalanceCBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.raw_giftbalance_c, parent, false)
            vh = EatmoreViewHolder(binding)
            return vh
        }
        else{
            val binding: RawGiftbalanceCBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.raw_giftbalance_c, parent, false)
            vh = RestaurantViewHolder(binding)
            return vh
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is EatmoreViewHolder) {
            val eatmoregiftcardinfo= list[parentPosition].eatmore_giftcards!![position]
            holder.binding.giftcardsInfo=eatmoregiftcardinfo
            holder.binding.util=BindDataUtils
            holder.binding.handler=myclickhandler
          //  holder.binding.valueTxt.text=String.format(context.getString(R.string.vaerdi_kr),BindDataUtils.convertCurrencyToDanishWithoutLabel(eatmoregiftcardinfo.value!!))
            holder.binding.restaurantName.text=eatmoregiftcardinfo.name
            ImageLoader.loadImageRoundCornerFromUrl(context = context,cornerSize = 32,fromFile = eatmoregiftcardinfo.app_icon,imageView = holder.binding.imageview)
            holder.binding.orderNow.visibility= View.GONE
            holder.binding.executePendingBindings()
        }else{
            val restaurantgiftcardinfo= list[parentPosition].restaurant_giftcards!![position]
           (holder as RestaurantViewHolder).binding.giftcardsInfo=restaurantgiftcardinfo
            holder.binding.util=BindDataUtils
            holder.binding.handler=myclickhandler
            holder.binding.valueTxt.text=String.format(context.getString(R.string.vaerdi_kr),BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurantgiftcardinfo.value!!))
            ImageLoader.loadImageRoundCornerFromUrl(context = context,cornerSize = 32,fromFile = restaurantgiftcardinfo.app_icon,imageView = holder.binding.imageview)
            holder.binding.restaurantName.text=restaurantgiftcardinfo.restaurant_name
            holder.binding.orderNow.visibility= View.VISIBLE
            holder.binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {

        if(list[parentPosition].giftType == Constants.EATMORE )
            return TYPE_EATMORE
        else
            return TYPE_RESTAURANT
    }


    private class EatmoreViewHolder(val binding: RawGiftbalanceCBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    private class RestaurantViewHolder(val binding: RawGiftbalanceCBinding) : RecyclerView.ViewHolder(binding.root) {
    }


    override fun getItemCount(): Int {

        if(list[parentPosition].giftType == Constants.EATMORE )
            return list[parentPosition].eatmore_giftcards!!.size
        else
            return list[parentPosition].restaurant_giftcards!!.size

    }






}
