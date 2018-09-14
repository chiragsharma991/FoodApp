package dk.eatmore.foodapp.activity.main.epay.fragment


import android.databinding.DataBindingUtil
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.bambora.nativepayment.handlers.BNPaymentHandler
import com.bambora.nativepayment.interfaces.ICardRegistrationCallback
import com.bambora.nativepayment.managers.CreditCardManager
import com.bambora.nativepayment.models.creditcard.CreditCard
import com.bambora.nativepayment.network.RequestError
import com.bambora.nativepayment.widget.edittext.CardFormEditText
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_edit_cart.*
import java.util.HashMap
import android.text.Editable
import android.text.TextWatcher
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.transaction_status.*


class EditCart : BaseFragment(), ICardRegistrationCallback, CardFormEditText.IOnValidationEventListener {


    private val inputValidStates = HashMap<EditText, Boolean>()
    private lateinit var clickEvent: MyClickHandler
    private lateinit var binding: FragmentEditCartBinding


    companion object {

        val TAG = "EditCart"
        fun newInstance(): EditCart {
            return EditCart()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_edit_cart
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            clickEvent =MyClickHandler(this)
            binding.handlers=clickEvent
            edit_card_view.visibility=View.VISIBLE
        //    processDialog.visibility=View.GONE
            transaction_status.visibility=View.GONE
            secure_payment_btn.setEnabled(false)
            card_number_edt.requestFocus() ; secure_payment_btn.text="Enter valid card number"
            name_on_card.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {
                    validationCard()
                }
            })

            val BNPaymentBuilder = BNPaymentHandler.BNPaymentBuilder(context)
                    .merchantAccount("T638003301")
                    .debug(true)

            BNPaymentHandler.setupBNPayments(BNPaymentBuilder)

            card_number_edt.setValidationListener(this)
            expiry_date_edt.setValidationListener(this)
            security_code_edt.setValidationListener(this)
            secure_payment_btn.setOnClickListener(onRegisterButtonClickListener)




            inputValidStates[card_number_edt] = false
            inputValidStates[expiry_date_edt] = false
            inputValidStates[security_code_edt] = false


        } else {
            logd(TAG, "saveInstance NOT NULL")

        }

    }




    private fun updateButtonState() {
        var enabled = true
        for (key in inputValidStates.keys) {
            if (!inputValidStates[key]!!) {
                enabled = false
                break
            }
        }
        loge("name check",""+TextUtils.isEmpty(name_on_card.text.trim()))
        secure_payment_btn.setEnabled(enabled && !TextUtils.isEmpty(name_on_card.text.trim()))
        secure_payment_btn.alpha=if(enabled && !TextUtils.isEmpty(name_on_card.text.trim())) 1.0F else 0.5F
    }


    private val onRegisterButtonClickListener = View.OnClickListener {
    /*    EpayActivity.amIFinish=false
        edit_card_view.visibility=View.GONE
        processDialog.visibility=View.VISIBLE
        transaction_status.visibility=View.GONE
        processDialog.startAnimation(rightToLeftAnimation(context!!))//your_view for mine is imageView
        (activity as EpayActivity).toolbar_layout.visibility=View.GONE
        hideKeyboard()
        Handler().postDelayed({

            Log.e("TAG", "onClick: " + card_number_edt.text.toString() + " " + expiry_date_edt.enteredExpiryMonth + " " + expiry_date_edt.getEnteredExpiryYear() + " " + security_code_edt.text.toString())
            BNPaymentHandler.getInstance().registerCreditCard(
                    context,
                    card_number_edt.text.toString(),
                    expiry_date_edt.getEnteredExpiryMonth(),
                    expiry_date_edt.getEnteredExpiryYear(),
                    security_code_edt.getText().toString(),
                    this@EditCart
            )


        },1000)*/


    }


    // Validation listner

    override fun onFocusChanged(view: EditText?, hasFocus: Boolean, inputValid: Boolean) {
        loge("Focus---", "" + hasFocus + " " + inputValid)
        if (hasFocus) {
            (view!!.parentForAccessibility as TextInputLayout).isErrorEnabled = false
            view.setTextColor(ContextCompat.getColor(context!!, R.color.black))

        } else if (!inputValid) {
            (view!!.parentForAccessibility as TextInputLayout).error = " "
            view.setTextColor(ContextCompat.getColor(context!!, R.color.red))
        }

    }

    override fun onInputValidated(view: EditText, inputValid: Boolean) {
        loge("onInputValidated---", "" + inputValid)

        inputValidStates[view] = inputValid
        validationCard()

    }

    fun validationCard(){

        if(!inputValidStates[card_number_edt]!!)
            secure_payment_btn.text="Enter valid card number"
        else if(!inputValidStates[expiry_date_edt]!!)
            secure_payment_btn.text="Enter date"
        else if(!inputValidStates[security_code_edt]!!)
            secure_payment_btn.text="Enter cvv"
        else if(TextUtils.isEmpty(name_on_card.text.trim()))
            secure_payment_btn.text="Enter name"
        else secure_payment_btn.text="Proceed to payment"
        updateButtonState()

    }



    // Ragistration callbacks

    override fun onRegistrationSuccess(creditcard: CreditCard?) {
/*
        loge("TAG", "success---")
        edit_card_view.visibility=View.GONE
        processDialog.visibility=View.GONE
        transaction_status.visibility=View.VISIBLE
        tx_status_float.setBackgroundColor(ContextCompat.getColor(context!!,R.color.green))
        transaction_status.startAnimation(rightToLeftAnimation(context!!))//your_view for mine is imageView
        tx_status_float.setImageResource(R.drawable.animated_vector_check)
        (tx_status_float.getDrawable() as Animatable).start()

        if(security_check.isChecked){
            BNPaymentHandler.getInstance().setCreditCardAlias(context,name_on_card.text.toString(),creditcard!!.creditCardToken,object :CreditCardManager.IOnCreditCardSaved{
                override fun onCreditCardSaved(p0: CreditCard?) {
                    loge("credit card saved...","")
                }
            })
        }
        //paymentHandler.setCreditCardAlias(this, aliasInput, creditCard.getCreditCardToken(), null)

*/

    }

    override fun onRegistrationError(p0: RequestError?) {
        loge("TAG", "Request error---")
    /*    edit_card_view.visibility=View.GONE
        processDialog.visibility=View.GONE
        transaction_status.visibility=View.VISIBLE
        tx_status_float.setBackgroundColor(ContextCompat.getColor(context!!,R.color.red))
        transaction_status.startAnimation(rightToLeftAnimation(context!!))//your_view for mine is imageView
        tx_status_float.setImageResource(R.drawable.animated_vector_cross)
        (tx_status_float.getDrawable() as Animatable).start()*/

    }


    //--//


    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }


     class  MyClickHandler(val editcart: EditCart) {


        fun backToOrder(view: View) {
            //loge("TAG","click---")
            Log.e("TAG","click---")
            (editcart.activity as EpayActivity).finishActivity()
        }

    }


}

