package dk.eatmore.foodapp.adapter.UniversalAdapter

import dk.eatmore.foodapp.model.User

interface RecyclerClickInterface {
    fun onClick(user: User)
}

interface RecyclerClickListner{

    fun <T> onClick(model : T?)
}
