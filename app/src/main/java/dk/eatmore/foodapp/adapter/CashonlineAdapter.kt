package dk.eatmore.foodapp.adapter



import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import android.view.View
import android.util.Log
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.epay.fragment.Paymentmethod
import dk.eatmore.foodapp.databinding.RowPaybalanceBinding
import dk.eatmore.foodapp.databinding.RowPaymethodBinding
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants
import kotlin.collections.ArrayList


class CashonlineAdapter(val context: Context, val list: ArrayList<Paymentmethod.PaymentInfoModel>, val myclickhandler: Paymentmethod.MyClickHandler, val paymentmethod: Paymentmethod, var canichangeSegment: Boolean, val appliedgift_list: ArrayList<Paymentmethod.AppliedGiftModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var count: Int = 1
    private val TYPE_BALANCE = 0
    private val TYPE_ITEM = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null

        if(viewType == TYPE_BALANCE){
            val binding: RowPaybalanceBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_paybalance, parent, false)
            vh = MyHeaderViewHolder(binding)
            return vh
        }else{
            val binding: RowPaymethodBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_paymethod, parent, false)
            vh = MyViewHolder(binding,MyCustomEditTextListener())
            return vh
        }

    }

    override fun getItemViewType(position: Int): Int {
        if(list[position].payment_type == Constants.EATMORE || list[position].payment_type == Constants.RESTAURANT)
            return TYPE_BALANCE
        else
            return TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.e("TAG"," on bind -"+position)
        if (holder is MyViewHolder) {

            holder.binding.paymentinfo=list[position]
            holder.myCustomEditTextListener.updatePosition(position)
            holder.binding.handlers=myclickhandler
            holder.binding.promotioncodeEdt.setText(list.get(position).edittextvalue)
            holder.binding.errorOfPromotioncode.text=list[position].promotioncodeMsg
            holder.binding.errorOfPromotioncode.setTextColor(if(list[position].errorOfPromotioncode) ContextCompat.getColor(context, R.color.theme_color) else ContextCompat.getColor(context, R.color.green))



            holder.binding.paymentSelection.setOnClickListener{

                if(Paymentmethod.anythingisInprogress)return@setOnClickListener  // if any progress is running

                // click on title and change segment

                if(canichangeSegment){


                    for (i in 0.until(list.size)){
                        list[i].view_expand=false
                        list[i].error_expand=false
                    }
                    list[position].view_expand=true
                    Paymentmethod.whatisthePaymethod=if(list[position].payment_type == context.getString(R.string.online_payment)) Paymentmethod.WhatIsThePaymethod.ONLINE else Paymentmethod.WhatIsThePaymethod.CASH

                  //  list[position].error_expand=if(holder.binding.errorOfPromotioncode.visibility == View.VISIBLE) true else false  // if you have error and click on same that
                    if(holder.binding.errorOfPromotioncode.visibility == View.VISIBLE){
                        // ignore if same segment click and gift applied
                        list[position].error_expand = true
                    }else{
                        list[position].error_expand = false

                        paymentmethod.cpn_discount_amount=0.0
                        paymentmethod.cpn_discount_id=""
                        paymentmethod.cpn_discount_type=""

                        holder.binding.promotioncodeEdt.text.clear()
                        holder.binding.promotioncodeEdt.clearFocus()
                        list[position].canishowpromotionMsg = false

                        // change data defult to change segment .
                        paymentmethod.showproductInfo(EpayFragment.ui_model!!.viewcard_list.value?.result, EpayFragment.paymentattributes.discount_amount, EpayFragment.paymentattributes.discount_type,EpayFragment.paymentattributes.discount_id,false)
                    }


                }

            }

            holder.binding.applypromotionBtn.setOnClickListener{

                if(Paymentmethod.anythingisInprogress)return@setOnClickListener  // if any progress is running

                if(list[position].edittextvalue.trim().length <= 0) return@setOnClickListener // if edit text have no value

                for (i in 0.until(list.size)){
                    list[i].gift_loader=false
                }
                list[position].gift_loader=true
                Paymentmethod.anythingisInprogress = true
                notifyDataSetChanged() // for loader only
                paymentmethod.applygiftcoupan(holder.binding,list[position])




            }


            holder.binding.executePendingBindings()


        }else{
            (holder as MyHeaderViewHolder).binding.paymentinfo=list[position]
            val builder = SpannableStringBuilder()
            val span1 = SpannableString("Use ${list[position].payment_type} balance ")
            val span2 = SpannableString("(${BindDataUtils.convertCurrencyToDanishWithoutLabel(list[position].balance)})")
            span2.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.theme_color)), 0, span2.trim().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(span1).append(span2)
            holder.binding.giftTitle.text=builder
            holder.binding.handlers=myclickhandler

            holder.binding.giftItem.setOnClickListener{



                if(!canichangeSegment && list[position].ischeck == false){
                    // block other checkbox to perform check

                }else{

                    if(Paymentmethod.anythingisInprogress)return@setOnClickListener  // if any progress is running

                    // allow check box to check

                    if(holder.binding.giftCheck.isChecked) {

                        //  Unchecked

                        list[position].ischeck=false

                        val iterator = appliedgift_list.iterator()
                        while (iterator.hasNext()) {
                            val appliedgiftmodel = iterator.next()
                            if(appliedgiftmodel.gift_type == list[position].payment_type){
                                iterator.remove()
                            }
                        }

                        setpaymentMethod()

                    }
                    else{

                        // Checked

                        list[position].ischeck = true

                        // appliedgift_list manage to set text from selected wallet balance

                        appliedgift_list.add(Paymentmethod.AppliedGiftModel(
                                gift_type = list[position].payment_type,
                                actual_gift_value = list[position].balance,
                                applied_gift_value = 0.0
                        ))

                        setpaymentMethod()
                    }




                }
                 paymentmethod.showproductInfo(EpayFragment.ui_model!!.viewcard_list.value?.result, EpayFragment.paymentattributes.discount_amount, EpayFragment.paymentattributes.discount_type,EpayFragment.paymentattributes.discount_id,true)

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
   private  class MyHeaderViewHolder( val binding: RowPaybalanceBinding) : RecyclerView.ViewHolder(binding.root) {

        init {


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

    private fun setpaymentMethod () {

        /*
        * Using this method you can set additional price before the calculation so it is really helpful to identify that applied giftcard is real (eatmore balance > total to pay) or not
        *
        * Condition : =>
        *
        *-  additional charge may be : gift/online/cash depends on: (if whole payment is from gift then apply gift additional anotherwise : online/cash)
        *
        * if(eatmore balance > total to pay) Gift Additional charge else online/offline additional charge.
        *
        * This methods you have to test only when you apply any gift
        * */


        paymentmethod.cpn_discount_amount=0.0
        paymentmethod.cpn_discount_id=""
        paymentmethod.cpn_discount_type=""

        val appliedAdditionalprice = paymentmethod.getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble()
        val appliedfinalprice= paymentmethod.final_amount
        val priceWithoutAdditionalcharge= appliedfinalprice-appliedAdditionalprice

        var totalEatmore_balance = 0.0
        for (paymentInfoModel in list) {
            if (paymentInfoModel.ischeck) {
                totalEatmore_balance += paymentInfoModel.balance.toDouble()
            }
        }

        if(totalEatmore_balance >= (priceWithoutAdditionalcharge + EpayFragment.paymentattributes.additional_charges_giftcard.trim().toDouble())){
            // selection is actual gift card
            Paymentmethod.whatisthePaymethod = Paymentmethod.WhatIsThePaymethod.GIFT

        }else
        {
            // selection is not actual gift card it may be online/offline it is depend on default parms
            if(paymentmethod.defaultpaymentmethodType == context.getString(R.string.online_payment).trim())
                Paymentmethod.whatisthePaymethod = Paymentmethod.WhatIsThePaymethod.ONLINE
            else
                Paymentmethod.whatisthePaymethod = Paymentmethod.WhatIsThePaymethod.CASH
        }


    }


    override fun getItemCount(): Int {
        return list.size
    }







}
