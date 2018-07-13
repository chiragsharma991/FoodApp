package dk.eatmore.foodapp.utils

interface DrawableClickListener {

    enum class DrawablePosition {
        TOP, BOTTOM, LEFT, RIGHT
    }

    fun onClick(target: DrawablePosition)
}