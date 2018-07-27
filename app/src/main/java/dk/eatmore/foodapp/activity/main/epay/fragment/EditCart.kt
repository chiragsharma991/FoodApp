package dk.eatmore.foodapp.activity.main.epay.fragment


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
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
import com.bambora.nativepayment.utils.CompatHelper
import com.bambora.nativepayment.widget.edittext.CardFormEditText
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.UniversalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_edit_cart.*
import kotlinx.android.synthetic.main.native_card_registration_form.view.*
import kotlinx.android.synthetic.main.notification_template_lines_media.view.*
import java.util.HashMap

class EditCart : BaseFragment(), ICardRegistrationCallback, CardFormEditText.IOnValidationEventListener {


    private val inputValidStates = HashMap<EditText, Boolean>()


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
//            expiry_date_edt
            //          security_code_edt

            // Setup BNPaymentHandler

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
        secure_payment_btn.setEnabled(enabled)
        secure_payment_btn.alpha=if(enabled) 1.0F else 0.5F
    }


    private val onRegisterButtonClickListener = View.OnClickListener {
        Log.e("TAG", "onClick: " + card_number_edt.text.toString() + " " + expiry_date_edt.enteredExpiryMonth + " " + expiry_date_edt.getEnteredExpiryYear() + " " + security_code_edt.text.toString())
        BNPaymentHandler.getInstance().registerCreditCard(
                context,
                card_number_edt.text.toString(),
                expiry_date_edt.getEnteredExpiryMonth(),
                expiry_date_edt.getEnteredExpiryYear(),
                security_code_edt.getText().toString(),
                this@EditCart
        )
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
        updateButtonState()
    }

    //---//


    // Ragistration callbacks

    override fun onRegistrationSuccess(creditcard: CreditCard?) {
        loge("TAG", "success---")
        BNPaymentHandler.getInstance().setCreditCardAlias(context,name_on_card.text.toString(),creditcard!!.creditCardToken,object :CreditCardManager.IOnCreditCardSaved{
            override fun onCreditCardSaved(p0: CreditCard?) {
                loge("credit card saved...","")
            }

        })

        //paymentHandler.setCreditCardAlias(this, aliasInput, creditCard.getCreditCardToken(), null)


    }

    override fun onRegistrationError(p0: RequestError?) {
        loge("TAG", "Request error---")

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

}

