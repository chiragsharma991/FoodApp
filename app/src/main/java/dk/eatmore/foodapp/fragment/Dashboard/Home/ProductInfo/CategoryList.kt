package dk.eatmore.foodapp.fragment.ProductInfo


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.CartActivity
import dk.eatmore.foodapp.activity.main.HomeActivity
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerClickInterface
import dk.eatmore.foodapp.adapter.UniversalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.databinding.RowCategoryListBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.TransitionHelper
import kotlinx.android.synthetic.main.category_list.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class CategoryList : BaseFragment(), RecyclerClickInterface {


    private lateinit var binding: FragmentAccountContainerBinding
    private var mAdapter: UniversalAdapter<User, RowCategoryListBinding>? = null
    private val userList = ArrayList<User>()


    companion object {

        val TAG = "CategoryList"
        fun newInstance(): CategoryList {
            return CategoryList()
        }

    }


    override fun getLayout(): Int {
        return R.layout.category_list
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)

        //   binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        // return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            val bundle=arguments
           // val title=if(arguments!=null) bundle!!.getString("TITLE","") else ""
           // val title= bundle?.getString("TITLE","") ?:""


            txt_toolbar.text=bundle?.getString("TITLE","") ?:""
            fillData()
            mAdapter = UniversalAdapter(context!!, userList, R.layout.row_category_list, object : RecyclerCallback<RowCategoryListBinding, User> {
                override fun bindData(binder: RowCategoryListBinding, model: User) {
                    setRecyclerData(binder, model)
                }
            })
            recycler_view_category.layoutManager = LinearLayoutManager(getActivityBase())
            recycler_view_category.adapter = mAdapter
        }else{
            logd(TAG,"saveInstance NOT NULL")
        }
    }

    override fun onClick(user: User) {

        val intent=Intent(activity,CartActivity::class.java)
        intent.putExtra("TITLE",user.name)
        val pairs: Array<Pair<View,String>> = TransitionHelper.createSafeTransitionParticipants(activity!!, true)
        val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, *pairs)
        startActivity(intent, transitionActivityOptions.toBundle())

    }


    private fun fillData() {
        val user1 = User()
        user1.name="Pizza"
        userList.add(user1)

        val user2 = User()
        user2.name="Coca-Cola"
        userList.add(user2)

        val user3 = User()
        user3.name="Deep-Pan"
        userList.add(user3)

        val user4 = User()
        user4.name="Brown stick"
        userList.add(user4)

        val user5 = User()
        user5.name="Choco Moko"
        userList.add(user4)


    }


    private fun setRecyclerData(binder: RowCategoryListBinding, model: User) {
        binder.user=model
        binder.handler=this
    }

    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }

    fun backpress(): Boolean {
        val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
        val homeFragment : HomeFragment =(fragmentof as HomeContainerFragment).getHomeFragment()
        (homeFragment.fragment as DetailsFragment).setPalette()
        (homeFragment.fragment as DetailsFragment).appbar.setExpanded(true,true)
        return true
    }



}