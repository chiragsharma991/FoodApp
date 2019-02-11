package dk.eatmore.foodapp.utils

class Test   {




    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val person1 = Person("joe", 25)
        }}


        class Person(var fName: String, var personAge: Int) {


            // initializer block
            init {

                var str = "Chirag:Mihir"
                str=str.replace(":",",",false)
               // System.out.println("Using : as a delimiter " + Arrays.toString(parts))
             //   println("First Name = $fName")
               // println("Age = $str")


                val s = "."
                val q = ","
                val w = ","
                val `as` = "12,00"
                val newstring = `as`.replace(s, w).replace(q, w)
                println("Age = $newstring")

/*
                val data = Data(pm_name = "test")
                val list = ArrayList<Data>()
                list.add(data)*/
            //    val user = User(type = 5,data = null)
             //   System.out.println("Out put>>>> "+user.name+" "+user.type+""+user.data)


            }
        }





 /*   data class User(
            val status: Boolean,
            val msg: String ,
            val payment_method_image_path: String ,
            val payment_method_thumbnail_logo: String ,
            val data: ArrayList<Data>

    ) {
        constructor(
                // ==1 means if you not pass value then assign by default.
                status: Boolean? ,
                msg: String? ,
                payment_method_image_path: String? ,
                payment_method_thumbnail_logo: String? ,
                data: ArrayList<Data>?
        ) : this (
                status=status,
                msg=msg,
                data = data?: arrayListOf(),
                payment_method_thumbnail_logo = "",
                payment_method_image_path = ""
        )


    }*/






    data class Data (
            val pm_name: String ="",
            val logo: String ="",
            var copy_logo: String =""

    )




}



