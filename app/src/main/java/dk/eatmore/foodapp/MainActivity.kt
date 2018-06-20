package dk.eatmore.foodapp

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import dk.eatmore.foodapp.databinding.ActivityMainBinding
import dk.eatmore.foodapp.model.User

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding:ActivityMainBinding= DataBindingUtil.setContentView(this,R.layout.activity_main)
        val user=User()
        user.email ="chiragsharma991@gmail.com"
        user.name ="charisharma"
        binding.user =user
        //test
    }
}
