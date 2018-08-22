package dk.eatmore.foodapp.activity.main.cart

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
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
import dk.eatmore.foodapp.activity.main.cart.fragment.Extratoppings
import dk.eatmore.foodapp.adapter.CartViewAdapter
import dk.eatmore.foodapp.fragment.ProductInfo.Menu
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.cart.Data
import dk.eatmore.foodapp.model.cart.ProductAttributeListItem
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.cart.ProductIngredientsItem
import dk.eatmore.foodapp.utils.BaseActivity
import java.util.*
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.ProductListModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.fragment_account_container.*
import kotlinx.android.synthetic.main.toolbar_plus.*
import kotlin.collections.ArrayList


class CartActivity : BaseActivity() {

    var transition: Transition? = null
    private val userList = ArrayList<User>()
    private var ui_model: UIModel? = null
    private lateinit var p_id: String
    private var mAdapter: CartViewAdapter? = null
    private var tagadapter: TagAdapter<String>? = null

    companion object {
        val TAG = "CartActivity"
        fun newInstance(): CartActivity {
            return CartActivity()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        loge(TAG, "on create...")
        super.onCreate(savedInstanceState)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_cart)
        initView(savedInstanceState)
    }


    private fun fetch_ProductDetailList() {

        callAPI(ApiCall.getProductDetails(
                r_token = Constants.R_TOKEN,
                r_key = Constants.R_KEY,
                p_id = p_id
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val productdetails = body as ProductDetails
                if (productdetails.status) {
                    ui_model!!.product_ingredients.value = productdetails.data.product_ingredients
                    ui_model!!.product_attribute_list.value = productdetails.data.product_attribute_list
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(clayout, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout, getString(R.string.internet_not_available))
                    }
                }
                //showProgressDialog()
            }
        })


    }


    private fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                product_ingredients.observe(this@CartActivity, Observer<ArrayList<ProductIngredientsItem>> {
                    refreshIngredients()
                })
                product_attribute_list.observe(this@CartActivity, Observer<ArrayList<ProductAttributeListItem>> {
                    refreshAttributes()
                })
            }


    private fun refreshIngredients() {
        loge(TAG, "refresh ---")

        val mVals = arrayOfNulls<String>(ui_model!!.product_ingredients.value!!.size)
        for (i in 0..ui_model!!.product_ingredients.value!!.size - 1) {
            mVals[i] = ui_model!!.product_ingredients.value!![i].i_name
        }

        tagadapter = object : TagAdapter<String>(mVals) {
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
        (tagadapter as TagAdapter<String>).setSelectedList(set)

        flowlayout.setOnSelectListener(TagFlowLayout.OnSelectListener { selectPosSet ->
            Log.e(TAG, "selected ---" + selectPosSet + " checked list " + set.toString())
            // ingredientsJsonArray = null;

        })


    }

    private fun refreshAttributes() {

        recycler_view_cart.apply {
            loge(TAG, "attr size is " + ui_model!!.product_attribute_list.value!!.size)
            mAdapter = CartViewAdapter(context!!, ui_model!!.product_attribute_list.value!!, object : CartViewAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {
                    loge(TAG, "click----" + parentView + " " + parentPosition + " " + chilPosition)
                    val fragment = Extratoppings.newInstance(parentPosition, chilPosition, ui_model!!)
                    addFragment(R.id.cart_container, fragment, Extratoppings.TAG, true)
                }
            })
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }


    private fun initView(savedInstanceState: Bundle?) {

        val title = intent.extras.getString("TITLE", "")
        p_id = intent.extras.getString("PID", "")
        txt_toolbar.text = title
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.close))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transition = buildEnterTransition()
            window.enterTransition = transition
        }
        ui_model = createViewModel()
        if (ui_model!!.product_attribute_list.value == null) {
            fetch_ProductDetailList()

        } else {
            refreshIngredients()
            refreshAttributes()

        }


        //  fillData()


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
        enterTransition.setDuration(300)
        enterTransition.slideEdge = Gravity.BOTTOM
        return enterTransition
    }


    private fun fillData() {
        val user1 = User()
        user1.name = "Small"
        userList.add(user1)

        val user2 = User()
        user2.name = "midium"
        userList.add(user2)

        val user3 = User()
        user3.name = "large"
        userList.add(user3)

        val user4 = User()
        user4.name = "extra large"
        userList.add(user4)


    }


    class UIModel : ViewModel() {

        var product_ingredients = MutableLiveData<ArrayList<ProductIngredientsItem>>()
        var product_attribute_list = MutableLiveData<ArrayList<ProductAttributeListItem>>()


    }


}
