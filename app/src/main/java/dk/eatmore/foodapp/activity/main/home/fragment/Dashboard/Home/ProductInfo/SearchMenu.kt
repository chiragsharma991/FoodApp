package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo

import android.databinding.DataBindingUtil
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.searchmenu.SearchlistParentAdapter
import dk.eatmore.foodapp.databinding.SearchMenuBinding
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DrawableClickListener.DrawablePosition
import dk.eatmore.foodapp.utils.DrawableClickListener
import kotlinx.android.synthetic.main.search_menu.*
import java.util.ArrayList
import android.text.Editable
import android.text.TextWatcher
import dk.eatmore.foodapp.model.home.DefaultAttributeValue
import dk.eatmore.foodapp.model.home.ProductAttributeItem
import dk.eatmore.foodapp.model.home.ProductListItem


class SearchMenu : BaseFragment() {

    private lateinit var binding: SearchMenuBinding
    private lateinit var mAdapter: SearchlistParentAdapter
    private lateinit var menu_list : ArrayList<MenuListItem>
    private lateinit var menu_list_filtered : ArrayList<MenuListItem>
    private var filtertask: AsyncTask<String, Void, String>?=null




    companion object {

        val TAG = "SearchMenu"
        var searchString: String=""
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
                }
            }
        })

        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
                if(filtertask ==null){
                    filtertask = FilterTask().execute(search_edt.text.toString())

                }else{
                    filtertask!!.cancel(true)
                    filtertask = FilterTask().execute(search_edt.text.toString())
                }
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int,
                                           arg2: Int, arg3: Int) {
            }

            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int,
                                       arg3: Int) {
            }
        })


    }




         inner class FilterTask() : AsyncTask<String, Void, String>() {

            override fun onPreExecute() {
            }

            override fun doInBackground(vararg params: String): String {
                var count=0
                val charString = params[0]
                loge(TAG,"do in ---"+charString)
                SearchMenu.searchString=charString.toLowerCase()
                if (charString.isEmpty()) {
                    menu_list_filtered = arrayListOf()
                } else {
                    val filteredList = ArrayList<MenuListItem>()
                    var i =0
                    while (i < menu_list.size  && !isCancelled()) {
                        loge(TAG,"is p canceled "+isCancelled)

                        val productlistitem = ArrayList<ProductListItem>()
                        var j =0
                        while (j < menu_list.get(i).product_list!!.size && !isCancelled()) {

                            if(menu_list.get(i).product_list!![j].p_name.toLowerCase().contains(charString.toLowerCase()) || menu_list.get(i).product_list!![j].p_desc.toLowerCase().contains(charString.toLowerCase())){
                                count++
                                val productattributeitem = ArrayList<ProductAttributeItem>()

                                if(menu_list.get(i).product_list!!.get(j).product_attribute ==null){

                                    productlistitem.add(ProductListItem("","", arrayListOf(), menu_list.get(i).product_list!![j].p_desc,if(menu_list.get(i).product_list!![j].p_price == null) "" else menu_list.get(i).product_list!![j].p_price,"", menu_list.get(i).product_list!![j].p_name,"","","","",""))
                                    filteredList.add(MenuListItem("","","","", menu_list.get(i).c_name,productlistitem))

                                }else{
                                    for (k in 0.until(menu_list.get(i).product_list!!.get(j).product_attribute.size) ){

                                        productattributeitem.add(ProductAttributeItem(
                                                menu_list.get(i).product_list!!.get(j).product_attribute.get(k).pam_id,
                                                menu_list.get(i).product_list!!.get(j).product_attribute.get(k).a_name,
                                                DefaultAttributeValue(
                                                        menu_list.get(i).product_list!!.get(j).product_attribute.get(k).default_attribute_value.pad_id,
                                                        menu_list.get(i).product_list!!.get(j).product_attribute.get(k).default_attribute_value.a_price,
                                                        menu_list.get(i).product_list!!.get(j).product_attribute.get(k).default_attribute_value.a_value
                                                )
                                        )
                                        )
                                    }
                                    productlistitem.add(ProductListItem("","", productattributeitem, menu_list.get(i).product_list!![j].p_desc,if(menu_list.get(i).product_list!![j].p_price == null) "" else menu_list.get(i).product_list!![j].p_price,"", menu_list.get(i).product_list!![j].p_name,"","","","",""))
                                    filteredList.add(MenuListItem("","","","", menu_list.get(i).c_name,productlistitem))
                                }

                            }
                            ++j

                        }
                        ++i
                    }
                    menu_list_filtered = filteredList
                }
                return count.toString()
            }
            override fun onPostExecute(result: String) {
                loge(TAG,"on post ---"+result)
                if(menu_list_filtered.size > 0){
                    error_txt.visibility=View.GONE

                }else{
                    error_txt.visibility=View.VISIBLE
                    if(search_edt.text.length >0)
                        error_txt.text=getString(R.string.no_data_found)
                    else
                        error_txt.text=getString(R.string.please_search_the_item)
                }
                mAdapter.refreshlist(menu_list_filtered)


            }
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

