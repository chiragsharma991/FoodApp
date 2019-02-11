package dk.eatmore.foodapp.utils

import android.databinding.BindingAdapter
import android.net.ParseException
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.model.cart.ProductAttributeValueItem
import dk.eatmore.foodapp.model.home.ProductListItem
import java.text.NumberFormat
import java.util.*
import android.view.ViewGroup
import java.text.SimpleDateFormat
import android.R.attr.src
import android.graphics.*
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.File
import android.R.attr.src
import android.databinding.Bindable
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.Color.parseColor
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.AppCompatImageView


object BindDataUtils {

/*
    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }
*/



    fun convertCurrencyToDanish(amount: String): String? {

        try{
            val deviceLocale = Locale.getDefault().language //  if (deviceLocale.equalsIgnoreCase("en")) {
            //      return formatValueToMoney(amount);
            //  } else {

            val decimal_price=amount.replace(",",".").replace(".",".")
            val mNumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
            var convertedAmount: String? = mNumberFormat.format(decimal_price.toDouble())
            println(convertedAmount)
            if (convertedAmount != null && convertedAmount.length > 2) {
                convertedAmount = convertedAmount.substring(0, convertedAmount.length - 2)
            }
            return convertedAmount+" kr."

        }catch (e : Exception){
            Log.e("BindDataUtils","Exception error---"+e.message)
            return "null"
        }

        //  }
    }
    fun convertCurrencyToDanishWithoutLabel(amount: String): String? {

        try{
            val deviceLocale = Locale.getDefault().language //  if (deviceLocale.equalsIgnoreCase("en")) {
            //      return formatValueToMoney(amount);
            //  } else {
            val decimal_price=amount.replace(".",".").replace(",",".")
            val mNumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
            var convertedAmount: String? = mNumberFormat.format(decimal_price.toDouble())
            println(convertedAmount)
            if (convertedAmount != null && convertedAmount.length > 2) {
                convertedAmount = convertedAmount.substring(0, convertedAmount.length - 2)
            }
            return convertedAmount
            //  }

        }catch (e : Exception){
            Log.e("BindDataUtils","Exception error---"+e.message)
            return "null"
        }

    }

    fun convertImagetoBlackwhite(myBitmap : Bitmap) : Bitmap{

        val width = myBitmap.getWidth()
        val height = myBitmap.getHeight()

        val bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val factor = 255f
        val redBri = 0.2126f
        val greenBri = 0.2126f
        val blueBri = 0.0722f

        val length = width * height
        val inpixels = IntArray(length)
        val oupixels = IntArray(length)

        myBitmap.getPixels(inpixels, 0, width, 0, 0, width, height)

        var point = 0
        for (pix in inpixels) {
            val R = pix shr 16 and 0xFF
            val G = pix shr 8 and 0xFF
            val B = pix and 0xFF

            val lum = redBri * R / factor + greenBri * G / factor + blueBri * B / factor

            if (lum > 0.4) {
                oupixels[point] = -0x1
            } else {
                oupixels[point] = -0x1000000
            }
            point++
        }
        bmOut.setPixels(oupixels, 0, width, 0, 0, width, height)
        return bmOut
    }


    fun calculateRatingline() : Float {
      return 70f

    }

    fun parseDateToddMMyyyy(time:String):String {
        val inputPattern = "yyyy-MM-dd HH:mm:ss"
        val outputPattern = "dd-MMM-yyyy HH:mm"
     //   val outputPattern = "dd-MMM-yyyy h:mm a"
        val inputFormat = SimpleDateFormat(inputPattern)
        val outputFormat = SimpleDateFormat(outputPattern)
        var date:Date? = null
        var str:String? = null
        try
        {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
        }
        catch (e: ParseException) {
            e.printStackTrace()
        }
        return str!!
    }
    fun parsewithoutTimeToddMMyyyy(time:String):String {
        val inputPattern = "yyyy-MM-dd HH:mm:ss"
        val outputPattern = "dd-MM-yyyy"
     //   val outputPattern = "dd-MMM-yyyy h:mm a"
        val inputFormat = SimpleDateFormat(inputPattern)
        val outputFormat = SimpleDateFormat(outputPattern)
        var date:Date? = null
        var str:String? = null
        try
        {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
        }
        catch (e: ParseException) {
            e.printStackTrace()
        }
        return str!!
    }
    fun parseTimeToHHmm(time:String):String {
        val inputPattern = "yyyy-MM-dd HH:mm:ss"
        val outputPattern = "HH:mm"
     //   val outputPattern = "dd-MMM-yyyy h:mm a"
        val inputFormat = SimpleDateFormat(inputPattern)
        val outputFormat = SimpleDateFormat(outputPattern)
        var date:Date? = null
        var str:String? = null
        try
        {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
        }
        catch (e: ParseException) {
            e.printStackTrace()
        }
        return str!!
    }


    private val vibrantLightColorList = arrayOf(ColorDrawable(Color.parseColor("#F0EFF5")), ColorDrawable(Color.parseColor("#F0EFF5")), ColorDrawable(Color.parseColor("#F0EFF5")), ColorDrawable(Color.parseColor("#F0EFF5")), ColorDrawable(Color.parseColor("#F0EFF5")), ColorDrawable(Color.parseColor("#F0EFF5")), ColorDrawable(Color.parseColor("#F0EFF5")), ColorDrawable(Color.parseColor("#F0EFF5")))

    fun getRandomDrawbleColor(): ColorDrawable {
        val idx = Random().nextInt(vibrantLightColorList.size)
        return vibrantLightColorList[idx]
    }

    fun showOrderstatus(payment_status : String, order_status : String, enable_rating : Boolean) : String {


        if(payment_status.toLowerCase() == Constants.REFUNDED){

            return "Ordre er refunderet"

        }else if(enable_rating == true && order_status.toLowerCase() == Constants.ACCEPTED){

            return "lav en bedømmelse"

        } else if(enable_rating == false && order_status.toLowerCase() == Constants.ACCEPTED){

            return "Din bedømmelse:"

        } else if(order_status.toLowerCase() == Constants.REJECTED){

            return "Ordre er anulleret"

        } else if(enable_rating == true && order_status.toLowerCase() != Constants.ACCEPTED && order_status.toLowerCase() != Constants.REJECTED){

            return "Ordre under behandling"

        } else if(order_status.toLowerCase() == Constants.PENDING_OPENING_RESTAURANT){

            return "Ordre under behandling. Restauranten åbner kl. HH:MM"
        }else{

            return ""
        }

    }

}

