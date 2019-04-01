package dk.eatmore.foodapp.utils

import android.util.Log
import com.google.gson.internal.LinkedTreeMap

class Test   {




    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val person1 = Person("joe", 25)
        }}


    class Person(var fName: String, var personAge: Int) {


        // initializer block
        init {
            val value =" 1 2 "
            val productprice ="100"
            val discount = "10"
            val list = ArrayList<String>()
            list.add("2015")
            list.add("12")
            list.add("125 ")
            list.add("00 0")

            val is_present = if(list.contains(value.trim())) true else false
            val percentresult : Double =productprice.toDouble() - ((discount.toDouble() * productprice.toDouble())/100)
             val rountvalue = String.format("%.2f",percentresult)
            //Log.e("output is ",""+percentresult+" * "+is_present)
                println("output is = $rountvalue $is_present ")
          //  val user= User(msg = "",payment_method_image_path = "",payment_method_thumbnail_logo = "")
          //  println("output =  ${user.msg}")



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






        /*     data class Activity(
                     val pm_name: String,
                     val logo: String,
                     val copy_logo: String

             ) {
                 constructor(pm_name: String? ="",copy_logo: String?="",logo: String?="") : this("","","")
             }*/






        data class Data (
                val pm_name: String ="",
                val logo: String ="",
                var copy_logo: String =""

        )







    }
}



