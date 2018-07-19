package dk.eatmore.foodapp.activity.main

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.LinearLayoutManager
import android.transition.Slide
import android.transition.Transition
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.CartViewAdapter
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseActivity
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

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
        val mVals = arrayOfNulls<String>(10)
        for (i in 0..9){
            mVals[i]="Test $i"
        }

        val tagadapter = object : TagAdapter<String>(mVals){
            override fun getView(parent: FlowLayout?, position: Int, t: String?): View {
                val tv = LayoutInflater.from(this@CartActivity).inflate(R.layout.ingredients_selct_layout,
                        flowlayout, false) as TextView
                tv.text = t
                return tv

            }

        }
        flowlayout.setAdapter(tagadapter)
        val set = HashSet<Int>()
        for (j in mVals.indices) {
            set.add(j)
        }
        tagadapter.setSelectedList(set)

        flowlayout.setOnSelectListener(TagFlowLayout.OnSelectListener { selectPosSet ->
            Log.e(TAG, "selected ---" + selectPosSet)
            // ingredientsJsonArray = null;

        })


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
