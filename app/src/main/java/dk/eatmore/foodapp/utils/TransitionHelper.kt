package dk.eatmore.foodapp.utils

import android.app.Activity
import android.os.Build
import android.support.v4.util.Pair
import android.view.View

import java.util.ArrayList
import java.util.Arrays

/**
 * Helper class for creating content transitions used with [android.app.ActivityOptions].
 */
internal object TransitionHelper {

    /**
     * Create the transition participants required during a activity transition while
     * avoiding glitches with the system UI.
     *
     * @param activity The activity used as start for the transition.
     * @param includeStatusBar If false, the status bar will not be added as the transition
     * participant.
     * @return All transition participants.
     */
    fun createSafeTransitionParticipants(activity: Activity,
                                         includeStatusBar: Boolean, vararg otherParticipants: Pair<View,String>): Array<Pair<View, String>> {
        // Avoid system UI glitches as described here:
        // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
        val decor = activity.window.decorView
        var statusBar: View? = null
        if (includeStatusBar) {
            statusBar = decor.findViewById(android.R.id.statusBarBackground)
        }
        val navBar = decor.findViewById<View>(android.R.id.navigationBarBackground)

        // Create pair of transition participants.
        val participants: ArrayList<Pair<View, String>> = ArrayList<Pair<View, String>>(3)
        addNonNullViewToTransitionParticipants(statusBar, participants)
        addNonNullViewToTransitionParticipants(navBar, participants)
        // only add transition participants if there's at least one none-null element
        if (otherParticipants != null && !(otherParticipants.size == 1 && otherParticipants[0] == null)) {
            participants.addAll(Arrays.asList<Pair<View,String>>(*otherParticipants))  // [] convert to Array
        }
        return participants.toTypedArray<Pair<View, String>>()  // [] typed array
    }

    private fun addNonNullViewToTransitionParticipants(view: View?, participants: MutableList<Pair<View,String>>) {
        if (view == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            participants.add(Pair(view, view.transitionName))
        }
    }

}
