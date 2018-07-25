package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerClickInterface
import dk.eatmore.foodapp.adapter.UniversalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentOrderContainerBinding
import dk.eatmore.foodapp.databinding.RowOrderedPizzaBinding
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedRestaurant
import dk.eatmore.foodapp.model.OrderFragment.UI_OrderFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_order_container.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.ArrayList

class OrderFragment : BaseFragment(), RecyclerClickInterface {


    private lateinit var binding: FragmentOrderContainerBinding
    private var mAdapter: UniversalAdapter<User, RowOrderedPizzaBinding>? = null
    private  var ui_model: UIModel?=null
    val refFragment: OrderFragment = this
    private val list = ArrayList<User>()




    companion object {

        val TAG= "OrderFragment"
        fun newInstance() : OrderFragment {
            return OrderFragment()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root


    }

    override fun getLayout(): Int {
        return R.layout.fragment_order_container
    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {

        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            txt_toolbar.text=getString(R.string.orders)
            recycler_view.apply {

                fillData()
                mAdapter = UniversalAdapter(context!!, list, R.layout.row_ordered_pizza, object : RecyclerCallback<RowOrderedPizzaBinding, User> {
                    override fun bindData(binder: RowOrderedPizzaBinding, model: User) {
                        binder.user=model
                        binder.handler=this@OrderFragment
                    } })
                layoutManager = LinearLayoutManager(getActivityBase())
                adapter = mAdapter
            }
        }else{
            logd(TAG,"saveInstance NOT NULL")
        }
/*

         ui_model = ViewModelProviders.of(this).get(UIModel::class.java)
        ui_model!!.getUIModel().observe(this, Observer<UI_OrderFragment>{
            loge(TAG,"observer success---")
            binding.uiOrder=ui_model!!.getUIModel().value
        })
        ui_model!!.init()
*/



    }

    override fun onClick(user: User) {
        val fragment = OrderedRestaurant.newInstance()
        addFragment(R.id.home_order_container,fragment, OrderedRestaurant.TAG,true)
    }



    private fun fillData() {
        val user1 = User()
        user1.name="Pronto Pizza"
        list.add(user1)

        val user2 = User()
        user2.name="Vegmix "
        list.add(user2)

        val user3 = User()
        user3.name="Pronto Pizza"
        list.add(user3)

        val user4 = User()
        user4.name="Brown stick"
        list.add(user4)

        val user5 = User()
        user5.name="Margherita Pizza "
        list.add(user4)


    }



    override fun onDestroy() {
        super.onDestroy()
        logd(TAG,"on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG,"on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG,"on pause...")

    }

}


private class UIModel : ViewModel(){

    var uiData = MutableLiveData<UI_OrderFragment>()

    fun init() {
        val ui_orderfragment= UI_OrderFragment("Order",false)
        uiData.value = ui_orderfragment
    }
    fun set(body: Any?) {
        /* expensive operation, e.g. network request */
        //uiData.value = (body as LastOrder)
    }

    fun getUIModel(): LiveData<UI_OrderFragment> {
        return uiData
    }


}



