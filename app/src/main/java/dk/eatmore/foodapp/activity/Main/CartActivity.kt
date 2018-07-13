package dk.eatmore.foodapp.activity.Main

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.LinearLayoutManager
import android.transition.Slide
import android.transition.Transition
import android.view.Gravity
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.CartViewAdapter
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseActivity
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.ArrayList

class CartActivity : BaseActivity() {

    var transition : Transition?=null
    private val userList = ArrayList<User>()
    private  var mAdapter: CartViewAdapter?=null


    companion object {
        val TAG="CartActivity"
        fun newInstance() : CartActivity {
            return CartActivity()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        val title=intent.extras.getString("TITLE","")
        txt_toolbar.text=title
        fullScreen()
        initView(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transition = buildEnterTransition()
            window.enterTransition = transition
        }

    }

    private fun initView(savedInstanceState: Bundle?) {

        if(savedInstanceState==null){
            // if you not take in this condition than if you change orientation then fragment added again and again.
        }
        else{
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) toolbar.elevation =0.0f
        DrawableCompat.setTint(ContextCompat.getDrawable(this,R.drawable.close)!!, ContextCompat.getColor(this, R.color.theme_color));

        toolbar.setNavigationIcon(ContextCompat.getDrawable(this,R.drawable.close))
        toolbar.setNavigationOnClickListener{
            onBackPressed()
        }
        fillData()
        recycler_view_cart.apply {
            mAdapter = CartViewAdapter(context!!,userList,object: CartViewAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {
                    loge(TAG,"----"+parentView+" "+parentPosition+" "+chilPosition)
                }
            })
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }

    override fun onBackPressed() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            finishAfterTransition()
        else
            finish()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buildEnterTransition(): Transition {
        val enterTransition = Slide()
        enterTransition.setDuration(500)
        enterTransition.slideEdge = Gravity.BOTTOM
        return enterTransition
    }


    private fun fillData() {
        val user1 = User()
        user1.name="Small"
        userList.add(user1)

        val user2 = User()
        user2.name="midium"
        userList.add(user2)

        val user3 = User()
        user3.name="large"
        userList.add(user3)

        val user4 = User()
        user4.name="extra large"
        userList.add(user4)


    }


}
