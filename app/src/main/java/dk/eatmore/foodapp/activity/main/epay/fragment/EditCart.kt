package dk.eatmore.foodapp.activity.main.epay.fragment


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bambora.nativepayment.handlers.BNPaymentHandler
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_edit_cart.*
import kotlinx.android.synthetic.main.native_card_registration_form.view.*
import kotlinx.android.synthetic.main.notification_template_lines_media.view.*

class EditCart : BaseFragment() {

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
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
//            expiry_date_edt
  //          security_code_edt



        }else{
            logd(TAG,"saveInstance NOT NULL")

        }


//getEnteredExpiryMonth

    }

/*
    private val onRegisterButtonClickListener = View.OnClickListener {
        Log.e("TAG", "onClick: " + card_number_edt.text.toString() + " " + (expiry_date_edt as com.bambora.nativepayment.widget.edittext.ExpiryDateEditText).et_expiry_date + " " + expiryDateEditText.getEnteredExpiryYear() + " " + securityCodeEditText.getText().toString())
        BNPaymentHandler.getInstance().registerCreditCard(
                context,
                card_number_edt.text.toString(),
                expiryDateEditText.getEnteredExpiryMonth(),
                expiryDateEditText.getEnteredExpiryYear(),
                securityCodeEditText.getText().toString(),
                resultListener
        )
    }
*/



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

