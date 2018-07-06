package dk.eatmore.foodapp.adapter

import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.model.MenuRestaurant
import dk.eatmore.foodapp.model.User

object ViewHolderFactory {

    fun create(view: View, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.row_rating_list -> RatingViewHolder(view)
            R.layout.row_menu_restaurant -> MenuViewHolder(view)
            else -> {
                RatingViewHolder(view)
            }
        }
    }

    class RatingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<User> {

        var date: AppCompatTextView
        init {
            date = itemView.findViewById(R.id.date)
        }
        override fun bind(user : User) {
            date.text = user.name
        }
    }
    class MenuViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<MenuRestaurant> {

        var restaurant_name: AppCompatTextView
        init {
            restaurant_name= itemView.findViewById(R.id.menu_raw_txt)
        }
        override fun bind(menu : MenuRestaurant) {
            restaurant_name.text = menu.name
        }
    }



}
