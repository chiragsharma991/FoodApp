package dk.eatmore.foodapp.activity.main.home.fragment.ProductInfo

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.searchmenu.SearchlistParentAdapter
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.databinding.SearchMenuBinding
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DrawableClickListener.DrawablePosition
import dk.eatmore.foodapp.utils.DrawableClickListener
import kotlinx.android.synthetic.main.search_menu.*
import java.util.ArrayList
import android.databinding.adapters.SearchViewBindingAdapter.setOnQueryTextListener
import android.text.Editable
import android.text.TextWatcher






class SearchMenu : BaseFragment() {

    private lateinit var binding: SearchMenuBinding
    private lateinit var mAdapter: SearchlistParentAdapter
    private lateinit var menu_list : ArrayList<MenuListItem>
    private lateinit var menu_list_filtered : ArrayList<MenuListItem>



    companion object {

        val TAG = "SearchMenu"
        fun newInstance(menuList: ArrayList<MenuListItem>?): SearchMenu {
            val bundle=Bundle()
            val fragment = SearchMenu()
            bundle.putSerializable(Constants.MENULIST,menuList)
            fragment.arguments=bundle
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.search_menu
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
           //return inflater.inflate(getLayout(), container, false)

           binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
           return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            menu_list = arguments!!.getSerializable(Constants.MENULIST) as ArrayList<MenuListItem>
            menu_list_filtered= arrayListOf()
            loge(TAG,"array list is "+menu_list.size)
            search_edt.requestFocus()
            showKeyboard()
            keylistner()
            refreshview()


        }else{
            logd(TAG,"saveInstance NOT NULL")

        }



    }

    private fun keylistner() {

        search_edt.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawableClickListener.DrawablePosition.LEFT -> {
                        (activity as HomeActivity).onBackPressed()
                    }

                    else -> {
                        search_edt.text.clear()
                    }
                }//Do something here
            }
        })

        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
                mAdapter.filter.filter(search_edt.text.toString())
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int,
                                           arg2: Int, arg3: Int) {
            }

            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int,
                                       arg3: Int) {
            }
        })

    }

    private fun refreshview(){

        recycler_view_parent.apply {

            mAdapter = SearchlistParentAdapter(context!!,menu_list,menu_list_filtered, object : SearchlistParentAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {
                    loge(TAG,"clicked---")

                }
            })
            mAdapter.setHasStableIds(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

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

}

