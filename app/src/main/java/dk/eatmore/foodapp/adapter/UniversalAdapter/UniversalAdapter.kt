package dk.eatmore.foodapp.adapter.UniversalAdapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

/**
 *
 */
class UniversalAdapter<T, VM : ViewDataBinding>(private val context: Context, private val items: ArrayList<T>?, private val layoutId: Int, private val bindingInterface: RecyclerCallback<VM, T>)
    : RecyclerView.Adapter<UniversalAdapter<T,VM>.RecyclerViewHolder>() {

    inner class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var binding: VM? = null

        init {
            binding = DataBindingUtil.bind(view)
        }

        fun bindData(model: T) {
            bindingInterface.bindData(binding!!, model)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): RecyclerViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false)
        return RecyclerViewHolder(v)
    }

    override fun onBindViewHolder(holder: UniversalAdapter<T,VM>.RecyclerViewHolder, position: Int) {
        val item = items!![position]
        holder.bindData(item)
    }

    override fun getItemCount(): Int {
        return items?.size?: 0  // value or if you got null then 0
    }

}