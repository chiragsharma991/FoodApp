package dk.eatmore.foodapp.rest

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

internal object ApiClient{

  //  val BASE_URL = "https://eatmoredev.dk/restapi/v3/"
    val BASE_URL = "https://eatmore.dk/restapi/v3/"

    private var retrofit: Retrofit? = null


    fun getClient(): Retrofit? {


        var interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder().
                connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).
                addInterceptor(interceptor).build()

        if (retrofit == null) {
            //val builder = GsonBuilder().disableHtmlEscaping().create()


            retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build()
        }
        return retrofit
    }


}



