package dk.eatmore.foodapp.utils

import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.ChangePassword
import kotlinx.android.synthetic.main.test.*
import java.security.AccessController.getContext
import java.util.*
import kotlin.collections.ArrayList

class Test : BaseActivity() {




/*    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val person1 = Person("joe", 25)
        }}


        class Person(var fName: String, var personAge: Int) {


            // initializer block
            init {

                var str = "Chirag:Mihir"
                str=str.replace(":",",",false)
               // System.out.println("Using : as a delimiter " + Arrays.toString(parts))
             //   println("First Name = $fName")
                println("Age = $str")
            }
        }*/

    companion object {
        val TAG = "ChangePassword"
        fun newInstance(): ChangePassword {
            return ChangePassword()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Glide.with(this).asGif()
                .load(R.raw.screen_revised)
                .listener(object : RequestListener<GifDrawable>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                        loge(TAG,"onLoadFailed")
                        return false
                    }

                    override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        loge(TAG,"onResourceReady")
                        resource!!.setLoopCount(1)
                        return false
                    }

                })
                .into(image_view)


    }


    }



