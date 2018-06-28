package dk.eatmore.foodapp.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintSet
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.animation.AnticipateOvershootInterpolator
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.utils.BaseActivity
import kotlinx.android.synthetic.main.activity_splash.*

class Splash : BaseActivity() {

    companion object {

        val TAG = "Splash"
        fun newInstance(): Splash {
            return Splash()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        init(savedInstanceState)
    }

    fun init(savedInstancedState: Bundle?) {

        fullScreen()
      //  shimmer_view.startShimmerAnimation()
        //getCurrentVersion()
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.zoom_out)
       // img_logo.startAnimation(animation)





        /*       ImageLoader.loadImageFromResource(this, R.drawable.bg_gift_cards, spl_logo)
       spl_logo.translationY = -resources.getDimension(R.dimen._100sdp)
       spl_logo.alpha = 0F


       spl_logo.animate().alpha(1F).translationY(resources.getDimension(R.dimen._1sdp)).duration = 1000*/

        //  ImageLoader.loadImageFromResource(this, R.drawable.splash_bg, img_splash)

        Handler().postDelayed({
            // GetLatestVersion().execute()
         //   moveToLogin()
            showComponents()


        }, 3000)

    }

    private fun showComponents(){

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

             val constraintSet = ConstraintSet()
             constraintSet.clone(this, R.layout.activity_main)

             val transition = ChangeBounds()
             transition.interpolator = AnticipateOvershootInterpolator(1.0f)
             transition.duration = 1200

             TransitionManager.beginDelayedTransition(constraint, transition)
             constraintSet.applyTo(constraint) //here constraint is the name of view to which we are applying the constraintSet
        }


    }
}
