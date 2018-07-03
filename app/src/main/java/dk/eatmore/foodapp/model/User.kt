package dk.eatmore.foodapp.model

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.util.Log

data class User  (
        var name: String = "",
        var email: String=""
) /*: BaseObservable() {

    var _name: String @Bindable get() = name
        set(a) {
            Log.e("set value","---"+a+" or "+name)
            name = a
            notifyPropertyChanged(BR._name)
        }

}*/