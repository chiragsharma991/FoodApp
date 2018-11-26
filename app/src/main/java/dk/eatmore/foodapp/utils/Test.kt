package dk.eatmore.foodapp.utils

import android.support.v7.app.AppCompatActivity

class Test  {




    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val person1 = Person("joe", 25)
        }}


        class Person(var fName: String, var personAge: Int) {


            // initializer block
            init {
            /*    fName = "chirag suthar"
                personAge = personAge
*/
                println("First Name = $fName")
                println("Age = $personAge")
            }
        }
    }



