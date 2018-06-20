package dk.eatmore.foodapp.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class MainViewModel : ViewModel() {

     var uiData = MutableLiveData<User>()

    fun init() {
        /* expensive operation, e.g. network request */
        val user=User()
        user.name ="vaibhav"
        user.email ="holle holle"

        uiData.value = user
    }
    fun set(s: String, s1: String) {
        /* expensive operation, e.g. network request */
        val user=User()
        user.name =s
        user.email =s1

        uiData.value = user
    }

    fun getUser(): LiveData<User> {
        return uiData
    }
}