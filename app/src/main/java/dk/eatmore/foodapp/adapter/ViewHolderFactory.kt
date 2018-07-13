package dk.eatmore.foodapp.adapter

import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.model.CategoryRestaurant
import dk.eatmore.foodapp.model.MenuRestaurant
import dk.eatmore.foodapp.model.User

object ViewHolderFactory {


    fun create(view: View, viewType: Int, clicklistner : Clicklistner): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.row_rating_list -> RatingViewHolder(view,clicklistner)
            R.layout.row_menu_restaurant -> MenuViewHolder(view,clicklistner)
            R.layout.row_category_list -> CategoryViewHolder(view,clicklistner)
            else -> {
                RatingViewHolder(view,clicklistner)
            }
        }
    }

    class RatingViewHolder(itemView: View, clicklistner: Clicklistner) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<User> {
        val listner : Clicklistner = clicklistner
        var date: AppCompatTextView
        init {
            date = itemView.findViewById(R.id.date)
        }
        override fun bind(user : User) {
            date.text = user.name
        }
    }
    class MenuViewHolder (itemView: View,clicklistner: Clicklistner) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<MenuRestaurant> {
        val listner : Clicklistner = clicklistner
        var restaurant_name: AppCompatTextView
        init {
            restaurant_name= itemView.findViewById(R.id.menu_raw_txt)
        }
        override fun bind(menu : MenuRestaurant) {
            restaurant_name.text = menu.name
            restaurant_name.setOnClickListener{ listner.clickOn()}
        }
    }

    class CategoryViewHolder (itemView: View,clicklistner: Clicklistner) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<CategoryRestaurant> {
        val listner : Clicklistner = clicklistner
        var row_cat_cardview: CardView
        init {
            row_cat_cardview= itemView.findViewById(R.id.row_cat_cardview)
        }
        override fun bind(menu : CategoryRestaurant) {
          //  restaurant_name.text = menu.name
            row_cat_cardview.setOnClickListener{ listner.clickOn()}
        }
    }

    interface Clicklistner{
        fun clickOn()
    }



}
