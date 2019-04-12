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
            val list = ArrayList<ModelTest>()
            list.add(ModelTest(pm_name = "Chirag",logo = "chi123.."))
            list.add(ModelTest(pm_name = "Mohit",logo = "mohi123.."))
            list.add(ModelTest(pm_name = "Bipin",logo = "bipin123.."))
            list.add(ModelTest(pm_name = "Osman",logo = "osm123.."))


            //Log.e("output is ",""+percentresult+" * "+is_present)
            println("output is = ${list}  ${list.size}")

            for (model in list){
                if(model.pm_name == "Bipn"){
                    list.remove(model)
                }
            }

            println("after remove = ${list} ${list.size}")


        }
    }


    data class ModelTest(
            val pm_name: String,
            val logo: String

    )



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














        data class Data (
                val pm_name: String ="",
                val logo: String ="",
                var copy_logo: String =""

        )







    }
}



