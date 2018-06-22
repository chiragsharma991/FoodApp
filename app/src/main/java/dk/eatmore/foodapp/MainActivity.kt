package dk.eatmore.foodapp

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import dk.eatmore.foodapp.databinding.ActivityMainBinding
import android.arch.lifecycle.ViewModelProviders
import android.util.Log
import dk.eatmore.foodapp.model.MainViewModel
import dk.eatmore.foodapp.model.User
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    private lateinit var usersViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val user =User()

 /*       val binding:ActivityMainBinding= DataBindingUtil.setContentView(this,R.layout.activity_main)

        //test
         usersViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java!!)
        if(savedInstanceState ==null)
        usersViewModel.init()
            // update

        txt_click.setOnClickListener{
           // usersViewModel.count = 2
            usersViewModel.getUser().value!!.name="chari sharma"
            usersViewModel.getUser().value!!.email="chiragsharma991@gmail.com"
           // binding.user=usersViewModel
        }
        txt_click_two.setOnClickListener{
           // val user =User()
            val a=User()
            a.name ="chirag"
            a.email ="raja jrajk"

            usersViewModel.set("chirag","chirag sharakdsk")
           // usersViewModel.getUser().value!!.name="test"
                    //   binding.user=user


        }

        binding.user=usersViewModel.getUser().value
       // if(savedInstanceState ==null)
//

        usersViewModel.getUser().observe(this, Observer { user -> Log.e("TAG", user!!.email)

        binding.user= usersViewModel.getUser().value
*/
        }





    }





