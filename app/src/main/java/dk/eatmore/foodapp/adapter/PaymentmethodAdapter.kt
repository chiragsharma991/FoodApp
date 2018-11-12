package dk.eatmore.foodapp.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CalculateAttribute
import dk.eatmore.foodapp.adapter.cart.CartChildViewAdapter
import dk.eatmore.foodapp.databinding.RowCartViewBinding
import dk.eatmore.foodapp.databinding.RowPaymentmethodBinding
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.cart.ProductAttributeListItem
import eu.epay.library.EpayWebView
import eu.epay.library.PaymentResultListener
import java.util.ArrayList
import java.util.HashMap

class PaymentmethodAdapter(val c: Context,val list : Array<Int?> ,val callback: AdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(), PaymentResultListener {


    private val VIEW_ITEM = 1
    lateinit var listner : AdapterListener
    private val context :Context?=null
    private val epay : EpayWebView? = null
    private var expanditem : Int =999
    private val data = HashMap<String, String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        var vh :RecyclerView.ViewHolder?=null
            this.listner=callback
            val binding : RowPaymentmethodBinding  = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_paymentmethod,parent,false)
            vh = MyViewHolder(binding)

        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {

            when(position){
                0 ->{
                    Log.e("epay--","Holder 0")
                    holder.binding.paymenttypeTxt.text="Online payment"
                    holder.binding.webiview.visibility= if(expanditem ==0) View.VISIBLE else View.GONE
                    holder.binding.cashBtn.visibility= View.GONE
                    val paymentView = EpayWebView(this, holder.binding.webiview, false)
                    var webview= holder.binding.webiview
                    webview = paymentView.LoadPaymentWindow(getData())
                    holder.binding.upDownArrow.setImageResource(if(expanditem ==0) R.drawable.up_arrow else R.drawable.down_arrow)
                    holder.binding.paymentType.setOnClickListener{
                        if(expanditem ==0) expanditem=999 else expanditem =0
                        notifyDataSetChanged()
                    }

                }
                1->{
                    Log.e("epay--","Holder 1")
                    holder.binding.paymenttypeTxt.text="Cash payment"
                    holder.binding.webiview.visibility= View.GONE
                    holder.binding.cashBtn.visibility= if(expanditem ==1) View.VISIBLE else View.GONE
                    holder.binding.cashBtn.setOnClickListener{listner.itemClicked(false,position)}
                    holder.binding.upDownArrow.setImageResource(if(expanditem ==1) R.drawable.up_arrow else R.drawable.down_arrow)
                    holder.binding.paymentType.setOnClickListener{
                        if(expanditem ==1) expanditem=999 else expanditem =1
                        notifyDataSetChanged()
                    }

                }

            }

        }

    }


    private class MyViewHolder(val binding :RowPaymentmethodBinding) : RecyclerView.ViewHolder(binding.root)  {

    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface AdapterListener {
        fun itemClicked(parentView : Boolean , position : Int)
    }


    override fun PaymentWindowLoaded() {
        Log.e("epay--","PaymentWindowLoaded")
    }

    override fun PaymentAccepted(p0: MutableMap<String, String>?) {
        Log.e("epay--","PaymentAccepted")
    }

    override fun PaymentWindowCancelled() {
        Log.e("epay--","PaymentWindowCancelled")
    }

    override fun Debug(p0: String?) {
        Log.e("epay--","Debug")

    }

    override fun ErrorOccurred(p0: Int, p1: String?, p2: String?) {
        Log.e("epay--","ErrorOccurred")

    }

    override fun PaymentWindowLoading() {
        Log.e("epay--","PaymentWindowLoading")

    }

    override fun PaymentLoadingAcceptPage() {
        Log.e("epay--","PaymentLoadingAcceptPage")
    }

    fun getData(): Map<String, String> {

        data.put("merchantnumber", "8024206")

        //http://tech.epay.dk/en/specification#259
        data.put("currency", "DKK")

        //http://tech.epay.dk/en/specification#260
        data.put("amount", "8500")

        //Random r = new Random();
        //int tempOrderno = r.nextInt(80 - 65) + 61235;

        //http://tech.epay.dk/en/specification#261
        data.put("orderid", "57510")

        //http://tech.epay.dk/en/specification#262
        //data.put("windowid", "1");

        //http://tech.epay.dk/en/specification#263
        data.put("paymentcollection", "0")

        //http://tech.epay.dk/en/specification#264
        data.put("lockpaymentcollection", "0")

        //http://tech.epay.dk/en/specification#265
        //data.put("paymenttype", "1,2,3");

        //http://tech.epay.dk/en/specification#266
        data.put("language", "0")

        //http://tech.epay.dk/en/specification#267
        data.put("encoding", "UTF-8")

        //http://tech.epay.dk/en/specification#269
        //data.put("mobilecssurl", "");

        //http://tech.epay.dk/en/specification#270
        // Authorize
        data.put("instantcapture", "0")

        // Direct Capture
        //data.put("instantcapture", "1");

        //http://tech.epay.dk/en/specification#272
        //data.put("splitpayment", "0");

        //http://tech.epay.dk/en/specification#275
        //data.put("callbackurl", "");

        //http://tech.epay.dk/en/specification#276
        data.put("instantcallback", "1")

        //http://tech.epay.dk/en/specification#278
        //data.put("ordertext", "");

        //http://tech.epay.dk/en/specification#279
        //data.put("group", "group");

        //http://tech.epay.dk/en/specification#280
        //data.put("description", "");

        //http://tech.epay.dk/en/specification#281
        //data.put("hash", "");

        //http://tech.epay.dk/en/specification#282
        //data.put("subscription", "0");

        //http://tech.epay.dk/en/specification#283
        //data.put("subscriptionname", "0");

        //http://tech.epay.dk/en/specification#284
        //data.put("mailreceipt", "");

        //http://tech.epay.dk/en/specification#286
        //data.put("googletracker", "0");

        //http://tech.epay.dk/en/specification#287
        //data.put("backgroundcolor", "");

        //http://tech.epay.dk/en/specification#288
        //data.put("opacity", "");

        //http://tech.epay.dk/en/specification#289
        //data.put("declinetext", "");

        data.put("ownreceipt", "1")

        Log.e("payment raw data", "payment$data")

        return data
    }


}
