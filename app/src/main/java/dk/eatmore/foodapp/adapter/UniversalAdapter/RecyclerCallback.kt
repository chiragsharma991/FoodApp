package dk.eatmore.foodapp.adapter.UniversalAdapter

import android.databinding.ViewDataBinding

interface RecyclerCallback<VM : ViewDataBinding, T> {
    fun bindData(binder: VM, model: T)
}
