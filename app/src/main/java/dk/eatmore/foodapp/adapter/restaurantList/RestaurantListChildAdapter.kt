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
import android.R.attr.pivotY
import android.R.attr.pivotX
import android.databinding.Bindable
import android.graphics.Matrix
import android.widget.ImageView.ScaleType
import android.support.v4.view.ViewCompat.setTranslationY
import android.support.v4.view.ViewCompat.setTranslationX
import android.support.v4.view.ViewCompat.setRotation
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import dk.eatmore.foodapp.utils.BindDataUtils
import java.text.SimpleDateFormat
import android.graphics.Bitmap
import com.bumptech.glide.request.target.SimpleTarget
import android.R.attr.path
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.Transition
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.ImageLoader


class RestaurantListChildAdapter(val context: Context, val listner: RestaurantListParentAdapter.AdapterListener, val parentPosition: Int, val list: ArrayList<RestaurantList.StatusWiseRestaurant>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            /*      Glide.with(context)
                          .asBitmap()
                          .load(list.get(parentPosition).restaurant.get(position).app_icon)
                          .into(object : SimpleTarget<Bitmap>() {
                              override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                  holder.binding.imageview.setImageBitmap(BindDataUtils.convertImagetoBlackwhite(resource))
                              }

                          })*/

//            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(45, 0, RoundedCornersTransformation.CornerType.ALL)))

            ImageLoader.loadImageRoundCornerFromUrl(context = context,cornerSize = 32,fromFile = list.get(parentPosition).restaurant.get(position).app_icon,imageView = holder.binding.imageview)
            //  ViewCompat.setTransitionName(holder.binding.imageview, position.toString() + "vnhvn")
            holder.binding.itemIsNew.visibility = View.GONE
            holder.binding.ordertype = list.get(parentPosition).ordertype
            holder.binding.restaurant = list.get(parentPosition).restaurant.get(position)
            val restaurant = list.get(parentPosition).restaurant.get(position)
            if (restaurant.total_rating > 5) {
                restaurant.sort_fiveplus_rate = true
            } else {
                restaurant.sort_fiveplus_rate = false
            }
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            restaurant.sort_created_date = sdf.parse(restaurant.created_date.trim()).time

            // delivery present
            if (restaurant.delivery_present) {
                holder.binding.deliverypresentView.visibility = View.VISIBLE
                if ((restaurant.delivery_charge_title == "" || restaurant.delivery_charge_title == null) && (restaurant.delivery_charge?.toDouble() == 0.0)) {
                    holder.binding.deliveryPresent.text = "Gratis levering"
                    restaurant.sort_delivery_charge = 0.0
                    restaurant.sort_free_delivery = true
                } else {
                    val label = "Leveringspris \n ${restaurant.delivery_charge_title
                            ?: ""} ${BindDataUtils.convertCurrencyToDanish(restaurant.delivery_charge!!)}"
                    holder.binding.deliveryPresent.text = label
                    restaurant.sort_delivery_charge = restaurant.delivery_charge.toDouble()
                    restaurant.sort_free_delivery = false
                }
            } else {
                restaurant.sort_delivery_charge = 0.0
                holder.binding.deliverypresentView.visibility = View.INVISIBLE
            }

            //opening titles
            if (restaurant.opening_title.toLowerCase() == "lukket") {
                holder.binding.openingHour.text = restaurant.opening_title
            } else {
                val label = "${restaurant.opening_title} ${restaurant.time}"
                holder.binding.openingHour.text = label
            }

            //Minimum order
            if (restaurant.delivery_present) {
                holder.binding.minimumOrderView.visibility = View.VISIBLE
                if ((restaurant.minimum_order_price == null) || (restaurant.minimum_order_price == "")) {
                    restaurant.sort_min_order_price = 0.0
                    holder.binding.minimumOrderView.visibility = View.INVISIBLE
                } else {
                    val label = "Minimumordre: \n ${BindDataUtils.convertCurrencyToDanish(restaurant.minimum_order_price)}"
                    restaurant.sort_min_order_price = restaurant.minimum_order_price.toDouble()
                    holder.binding.minimumOrder.text = label
                    holder.binding.minimumOrderView.visibility = View.VISIBLE
                }
            } else {
                restaurant.sort_min_order_price = 0.0
                holder.binding.minimumOrderView.visibility = View.INVISIBLE
            }

            if (list.get(parentPosition).restaurant.get(position).is_fav) {
                holder.binding.favoriteBtn.setColorFilter(ContextCompat.getColor(context, R.color.theme_color))
            } else {
                holder.binding.favoriteBtn.setColorFilter(ContextCompat.getColor(context, R.color.gray))
            }

            holder.binding.rowChildItem.setOnClickListener {
                listner.itemClicked(false, parentPosition, position, Constants.CARD_VIEW)

            }
            holder.binding.orderNow.setOnClickListener {
                listner.itemClicked(false, parentPosition, position, Constants.CARD_VIEW)

            }
            holder.binding.favoriteBtn.setOnClickListener {
                listner.itemClicked(false, parentPosition, position, Constants.FAVORITE_VIEW)
            }
            holder.binding.kstatus = PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)
            holder.binding.executePendingBindings()
        }
    }


    private class MyViewHolder(val binding: RowRestaurantlistCBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun getItemCount(): Int {
        return list.get(parentPosition).restaurant.size
    }


}
