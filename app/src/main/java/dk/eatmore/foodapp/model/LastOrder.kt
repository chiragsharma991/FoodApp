package dk.eatmore.foodapp.model

import java.io.Serializable

data class LastOrder(var msg: String = "",
                     var Openinghours: List<OpeninghoursItem>? = null,
                     var status: Boolean = false) : ModelUtility()


data class OpeninghoursItem(val reason: String = "",
                            val action_by: String = "",
                            val is_deleted: String = "",
                            val is_activated: String = "",
                            val or_id: String = "",
                            val restaurant_id: String = "",
                            val action_dt: String = "")