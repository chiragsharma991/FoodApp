package dk.eatmore.foodapp.utils

import org.greenrobot.eventbus.EventBus

object GlobalBus {
    private var sBus: EventBus? = null

    val bus: EventBus
        get() {
            if (sBus == null)
                sBus = EventBus.getDefault()
            return sBus!!
        }
}


class ParsingEvents {

    // Event used to send message from fragment to activity.
    class EventFromRestaurantList

    // Event used to send message from activity to fragment.
    class EventItself


}
