package dk.eatmore.foodapp.adapter.universalAdapter

import dk.eatmore.foodapp.model.User

interface RecyclerClickInterface {
    fun onClick(user: User)
}

interface RecyclerClickListner{

    fun <T> onClick(model : T?)
}
