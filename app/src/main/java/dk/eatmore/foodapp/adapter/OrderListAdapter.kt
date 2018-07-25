package dk.eatmore.foodapp.adapter

import android.databinding.DataBindingUtil
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.transition.ChangeBounds
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.RowOrderPreferredBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment

class OrderListAdapter(val homefragment: HomeFragment, val callback: AdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val VIEW_ITEM = 1
    lateinit var listner : AdapterListener





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        var vh :RecyclerView.ViewHolder?=null
        if(viewType == VIEW_ITEM){
            this.listner=callback
           val binding :RowOrderPreferredBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_order_preferred,parent,false)
            vh = MyViewHolder(binding)
        }

        return vh!!
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            //val holder: MyViewHolder = holder
           // holder.init()
            holder.binding.rowOrderCardview.setOnClickListener {
                listner.itemClicked(position)
            }
            holder.binding.orderNow.setOnClickListener{
                val fragment = Address.newInstance()
                val enter : Slide?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    enter = Slide()
                    enter.setDuration(300)
                    enter.slideEdge = Gravity.RIGHT
                    fragment.enterTransition=enter
                }

                homefragment.addFragment(R.id.home_fragment_container,fragment,Address.TAG,false)

            }


        }

    }


    private  class MyViewHolder(val binding :RowOrderPreferredBinding) : RecyclerView.ViewHolder(binding.root)  {

    }







    override fun getItemCount(): Int {
        return 6
    }


    override fun getItemViewType(position: Int): Int {
        return VIEW_ITEM
    }


    interface AdapterListener {
        fun itemClicked(position : Int)
    }

}