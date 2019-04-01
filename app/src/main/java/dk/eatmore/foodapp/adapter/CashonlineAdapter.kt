package dk.eatmore.foodapp.adapter



import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import java.util.ArrayList
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import dk.eatmore.foodapp.activity.main.epay.fragment.Paymentmethod
import dk.eatmore.foodapp.databinding.RowPaymethodBinding


class CashonlineAdapter(val context: Context, val list: ArrayList<Paymentmethod.PaymentInfoModel>, val myclickhandler: Paymentmethod.MyClickHandler, val paymentmethod: Paymentmethod) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var count: Int = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null
        val binding: RowPaymethodBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_paymethod, parent, false)
        // binding.util= BindDataUtils
        vh = MyViewHolder(binding,MyCustomEditTextListener())


        return vh
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {

            holder.binding.paymentinfo=list[position]
            holder.myCustomEditTextListener.updatePosition(position)
            holder.binding.handlers=myclickhandler
            holder.binding.promotioncodeEdt.setText(list.get(position).edittextvalue)



            holder.binding.paymentSelection.setOnClickListener{
                // click on title

                if(!list.get(0).gift_loader && !list.get(1).gift_loader){

                    if(list.get(position).payment_type == context.getString(R.string.online_payment))
                    {
                        list[0].view_expand=true
                        list[1].view_expand=false
                        list[0].error_expand=if(holder.binding.errorOfPromotioncode.visibility == View.VISIBLE) true else false
                        list[1].error_expand=false
                        notifyDataSetChanged()
                    }
                    else
                    {
                        list[0].view_expand=false
                        list[1].view_expand=true
                        list[0].error_expand=false
                        list[1].error_expand=if(holder.binding.errorOfPromotioncode.visibility == View.VISIBLE) true else false
                        notifyDataSetChanged()
                    }
                }
            }

            holder.binding.applypromotionBtn.setOnClickListener{
                if(!list[0].gift_loader && !list[1].gift_loader && list.get(position).edittextvalue.trim() != ""){

                    if(list.get(position).payment_type == context.getString(R.string.online_payment)){
                        list[0].gift_loader=true
                        list[1].gift_loader=false
                        notifyDataSetChanged()
                        paymentmethod.applygiftcoupan(holder.binding,list[position])

                    }else{
                        list[0].gift_loader=false
                        list[1].gift_loader=true
                        notifyDataSetChanged()
                        paymentmethod.applygiftcoupan(holder.binding,list[position])
                    }
                }


            }
            holder.binding.executePendingBindings()


        }
    }


   private  class MyViewHolder( val binding: RowPaymethodBinding, val myCustomEditTextListener: MyCustomEditTextListener) : RecyclerView.ViewHolder(binding.root) {
        // each data item is just a string in this case


        init {
            myCustomEditTextListener.updatebinder(binding)
            binding.promotioncodeEdt.addTextChangedListener(myCustomEditTextListener)

        }
    }



    private inner class MyCustomEditTextListener : TextWatcher {

        private var position: Int = 0
        private lateinit var binding: RowPaymethodBinding

        fun updatePosition(position: Int) {
            this.position = position
        }
        fun updatebinder(binding: RowPaymethodBinding) {
            this.binding = binding
        }

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            // no op
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            list.get(position).edittextvalue=charSequence.toString()
        }

        override fun afterTextChanged(editable: Editable) {
            // no op
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }







}
