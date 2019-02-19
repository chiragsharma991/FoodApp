package dk.eatmore.foodapp.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.view.View
import android.view.WindowManager
import com.google.gson.internal.LinkedTreeMap
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.Splash
import kotlinx.android.synthetic.main.test.*
import kotlinx.android.synthetic.main.toolbar_plusone.*

class Test : BaseActivity() {



    companion object {
        val TAG = "Test"
        fun newInstance(): Test {
            return Test()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        Transparent Status Bar
  /*      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
           // window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
             //       WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }*/


        setContentView(R.layout.test)
        fullScreen()

       // setLightStatusBar(this)


/*        setSupportActionBar(anim_toolbar)
        if (supportActionBar != null)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)*/


       // collapsing_toolbar.setTitle("Test")

        //   log(TAG, "savedInstanceState..."+savedInstanceState)

       // txt_toolbar.text="Test eatmore"

        //tabs.addTab(tabs.newTab().setText("menu"))
     //   tabs.addTab(tabs.newTab().setText("info"))
      //  tabs.addTab(tabs.newTab().setText("rate"))

    }


    fun setLightStatusBar( activity: Activity) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            var flags = activity.getWindow().getDecorView().systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.getWindow().getDecorView().systemUiVisibility = flags
            activity.window.statusBarColor = Color.WHITE
        }
    }


/*
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val person1 = Person("joe", 25)
        }}


        class Person(var fName: String, var personAge: Int) {


            // initializer block
            init {


            //    println("Age = $newstring")
                val user= User(msg = "",payment_method_image_path = "",payment_method_thumbnail_logo = "")
                println("output =  ${user.msg}")



            }
        }




    data class User(
            val status: Boolean,
            val msg: String ,
            val payment_method_image_path: String ,
            val payment_method_thumbnail_logo: String,
            val data: ArrayList<Data>

    ) {
        constructor(
                 status: Boolean?=true,
                  msg: String?="" ,
                  payment_method_image_path: String? ="",
                  payment_method_thumbnail_logo: String?="",
                 data: ArrayList<Data>? = arrayListOf()

        ) : this (
                status?: true,
                "null msg",
                //data = data?: arrayListOf(),
                "",
                 "null with value",
             //    "null with value",
                arrayListOf()
        )






   *//*     data class Activity(
                val pm_name: String,
                val logo: String,
                val copy_logo: String

        ) {
            constructor(pm_name: String? ="",copy_logo: String?="",logo: String?="") : this("","","")
        }*//*






    data class Data (
            val pm_name: String ="",
            val logo: String ="",
            var copy_logo: String =""

    )







}*/
}



