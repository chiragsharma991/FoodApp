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
                println("Age = $str")
                val user = User(type = 5)
                System.out.println("Out put>>>> "+user.name+" "+user.type)


            }
        }

/*
    companion object {
        val TAG = "ChangePassword"
        fun newInstance(): ChangePassword {
            return ChangePassword()
        }
    }
*/


/*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

    */
/*    Glide.with(this).asGif()
                .load(R.raw.screen_revised)
                .listener(object : RequestListener<GifDrawable>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                        loge(TAG,"onLoadFailed")
                        return false
                    }

                    override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        loge(TAG,"onResourceReady")
                        resource!!.setLoopCount(1)
                        return false
                    }

                })
                .into(image_view)*//*



    }
*/

/*
    data  class  User (val name: String? ="default value",
                       val type: Int
    ){
        constructor(name: String?,type: Int) : this(name ?: "null not",type=5)
    }
*/



    data class User(
            val name: Int ,
            val type: Int
    ) {
        constructor(
                name: Int? = 5,
                type: Int? =7
        ) : this(
                name=name ?: 5 ,
                type=type ?: 4
        )


    }


    }



