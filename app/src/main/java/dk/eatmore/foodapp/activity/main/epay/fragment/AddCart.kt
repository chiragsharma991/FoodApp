package dk.eatmore.foodapp.activity.main.epay.fragment


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bambora.nativepayment.handlers.BNPaymentHandler
import com.bambora.nativepayment.managers.CreditCardManager
import com.bambora.nativepayment.models.creditcard.CreditCard
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerClickInterface
import dk.eatmore.foodapp.adapter.UniversalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_add_cart.*
import java.util.ArrayList

class AddCart : BaseFragment(), RecyclerClickInterface , CreditCardManager.IOnCreditCardRead {




    private lateinit var binding: FragmentAddCartBinding
    private lateinit var editCart_fragment: EditCart
    private var mAdapter: UniversalAdapter<CreditCard, RowCreditListBinding>? = null
    private val creditCards = ArrayList<CreditCard>()
    private lateinit var  paymentHandler: BNPaymentHandler




    companion object {

        val TAG = "AddCart"
        fun newInstance(): AddCart {
            return AddCart()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_add_cart
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            add_new_card.setOnClickListener{
                editCart_fragment = EditCart.newInstance()
                (activity as EpayActivity).addFragment(R.id.epay_container, editCart_fragment, EditCart.TAG, true)
            }

            paymentHandler = BNPaymentHandler.getInstance()
            initCreditCardList()
            paymentHandler.getRegisteredCreditCards(context, this)




        }else{
            logd(TAG,"saveInstance NOT NULL")

        }




    }


    private fun initCreditCardList() {
        mAdapter = UniversalAdapter(context!!, creditCards, R.layout.row_credit_list, object : RecyclerCallback<RowCreditListBinding, CreditCard> {
            override fun bindData(binder: RowCreditListBinding, model: CreditCard) {
                setRecyclerData(binder, model)
            }
        })
        recycler_view_card_list.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view_card_list.adapter = mAdapter


        /*  val listView = findViewById(R.id.lv_credit_cards) as ListView
          creditCardListAdapter = CardListAdapter(this, creditCards)
          listView.setOnItemClickListener(this)
          listView.setAdapter(creditCardListAdapter)*/
    }

    private fun setRecyclerData(binder: RowCreditListBinding, model: CreditCard) {
         binder.list=model
         binder.handler=this

    }

    override fun onCreditCardRead(creditCards: MutableList<CreditCard>?) {
        loge("credit list---",""+creditCards!!.size+" "+creditCards.get(0).alias)
        this.creditCards.clear()
        if (creditCards != null) {
            this.creditCards.addAll(creditCards)
        }
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onClick(user: User) {

    }





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

