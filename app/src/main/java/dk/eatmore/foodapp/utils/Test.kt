package dk.eatmore.foodapp.utils

import android.support.v7.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList

class Test  {




    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val person1 = Person("joe", 25)
        }}


        class Person(var fName: String, var personAge: Int) {


            // initializer block
            init {

                val str = "abdc , psdv ,sdvosdv, dfpbkdd"
                var parts : Array<String> = str.split((",").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                System.out.println("Using : as a delimiter " + Arrays.toString(parts))
                for(i in 0 until parts.size){

                    System.out.println("Using : as a delimiter " + parts.get(i).toString())
                }
            /*    parts = str.split(("d").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                System.out.println(Arrays.toString(parts))
                val str2 = "This is a string to tokenize"
                parts = str2.split((" ").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                System.out.println(Arrays.toString(parts))*/

                println("First Name = $fName")
                println("Age = $personAge")
            }
        }
    }



