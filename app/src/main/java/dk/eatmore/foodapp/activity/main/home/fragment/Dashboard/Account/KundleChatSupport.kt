package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account


import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.databinding.KundlesupportBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.toolbar.*
import zendesk.core.Zendesk
import zendesk.core.AnonymousIdentity
import android.content.Intent
import android.content.Intent.getIntent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.FacebookSdk.getApplicationContext
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.zendesk.service.ErrorResponse
import com.zendesk.service.ZendeskCallback
import com.zendesk.belvedere.Belvedere
import com.zendesk.belvedere.BelvedereCallback
import com.zendesk.belvedere.BelvedereResult
import com.zendesk.logger.Logger
import com.zendesk.util.StringUtils
import com.zopim.android.sdk.api.Chat
import com.zopim.android.sdk.api.ChatServiceBinder
import com.zopim.android.sdk.api.ZopimChat
import com.zopim.android.sdk.chatlog.ZopimChatLogFragment
import com.zopim.android.sdk.data.DataSource
import com.zopim.android.sdk.embeddable.ChatActions
import com.zopim.android.sdk.model.VisitorInfo
import com.zopim.android.sdk.prechat.ChatListener
import com.zopim.android.sdk.prechat.EmailTranscript
import com.zopim.android.sdk.prechat.PreChatForm
import com.zopim.android.sdk.prechat.ZopimChatFragment
import com.zopim.android.sdk.widget.ChatWidgetService
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.KundlechatsupportBinding
import dk.eatmore.foodapp.databinding.RowAttachedImgBinding
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.kundlesupport.*
import kotlinx.android.synthetic.main.zs_activity_help_center.*
import org.json.JSONObject
import zendesk.support.*
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern


class KundleChatSupport : BaseFragment()  {



    private lateinit var binding: KundlechatsupportBinding


    companion object {
        val TAG = "KundleChatSupport"
       // private val ACCOUNT_KEY = "f0fKcUY8IsdkY8SjQFsLYuRIfozlAycq"
        private val ACCOUNT_KEY = "ZwZVZGy6MsaeO3DDfBW5LKTTzZyq3LW8"


        fun newInstance(): KundleChatSupport {
            return KundleChatSupport()
        }

    }


    override fun getLayout(): Int {
        return R.layout.kundlechatsupport
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root


    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            txt_toolbar.text = getString(R.string.kundle_support)
            img_toolbar_back.setOnClickListener {
                backpress()
            }
            (activity as AppCompatActivity).setSupportActionBar(toolbar)



            // Enable logging
            Logger.setLoggable(true)

            // Sample breadcrumb
            ZopimChat.trackEvent("Application created")

            /**
             * Minimum chat configuration. Chat must be initialization before starting the chat.
             */
            ZopimChat.init(ACCOUNT_KEY).emailTranscript(EmailTranscript.DISABLED)


            // clear visitor info. Visitor info storage can be disabled at chat initialization
            val emptyVisitorInfo = VisitorInfo.Builder().build()
           // ZopimChat.setVisitorInfo(emptyVisitorInfo)

            ZopimChat.trackEvent("Started chat via sample fragment integration")


            /**
             * If starting activity while the chat widget is actively presented the activity will resume the current chat
             */
            val widgetWasActive = activity!!.stopService(Intent(context, ChatWidgetService::class.java))
            if (widgetWasActive) {
                loge(TAG,"widgetWasActive ")
                resumeChat()
                return
            }

            /**
             * We've received an intent request to resume the existing chat.
             * Resume the chat via {@link com.zopim.android.sdk.api.ZopimChat#resume(android.support.v4.app.FragmentActivity)} and
             * start the {@link ZopimChatLogFragment}
             */
            if (activity!!.getIntent() != null) {
                val action = activity!!.getIntent().getAction()

                if (ChatActions.ACTION_RESUME_CHAT == action) {
                    loge(TAG,"ACTION_RESUME_CHAT ")

                    resumeChat()
                    return
                }
            }

            /**
             * Attempt to resume chat. If there is an active chat it will be resumed.
             */
            val chat = ZopimChat.resume(activity)
            if (!chat.hasEnded()) {
                loge(TAG,"hasEnded- ")

                resumeChat()
                return
            }

            /**
             * Start a new chat
             */
            run {
                loge(TAG,"run----")
                // set pre chat fields as mandatory
                val preChatForm = PreChatForm.Builder()
                        .name(PreChatForm.Field.REQUIRED_EDITABLE)
                        .email(PreChatForm.Field.REQUIRED_EDITABLE)
                        .phoneNumber(PreChatForm.Field.REQUIRED_EDITABLE)
                        .department(PreChatForm.Field.REQUIRED_EDITABLE)
                        .message(PreChatForm.Field.REQUIRED_EDITABLE)
                        .build()
                // build chat config
                val config = ZopimChat.SessionConfig()
                        .preChatForm(preChatForm)
                // prepare chat fragment
                val fragment = ZopimChatFragment.newInstance(config)
                // show fragment

                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(R.id.chat_fragment_container, fragment, ZopimChatFragment::class.java.name)
                transaction.commit()
            }


        } else {
            logd(TAG, "saveInstance NOT NULL")
        }
    }

    /**
     * Resumes the chat and loads the [ZopimChatLogFragment]
     */
    private fun resumeChat() {
        loge(TAG,"resumeChat---")
        // find the retained fragment
        if (childFragmentManager.findFragmentByTag(ZopimChatLogFragment::class.java.name) == null) {
            val chatLogFragment = ZopimChatLogFragment()
            val transaction = childFragmentManager.beginTransaction()
            transaction.add(com.zopim.android.sdk.R.id.chat_fragment_container, chatLogFragment, ZopimChatLogFragment::class.java.name)
            transaction.commit()
        }
    }


    fun backpress(){

        val chat = ZopimChat.resume(activity)
        chat.endChat()
        chat.disconnect()
        activity!!.supportFragmentManager
                .beginTransaction()
                .remove(activity!!.supportFragmentManager.findFragmentByTag(ChatServiceBinder::class.java.name))
                .commit()

        val transaction = childFragmentManager.beginTransaction()
        val zopimchatlogfragment= childFragmentManager.findFragmentByTag(ZopimChatLogFragment::class.java.name)
        if(zopimchatlogfragment !=null){
            transaction.remove(zopimchatlogfragment)
            transaction.commit()
        }

        childFragmentManager.popBackStack()

        (parentFragment as Profile).childFragmentManager.popBackStack()

  /*      DialogUtils.openDialog(context = context!!,btnNegative = getString(R.string.cancel) , btnPositive = getString(R.string.end),color = ContextCompat.getColor(context!!, R.color.theme_color),msg = "Your chat session will be closed",title = "End this chat?",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
            override fun onPositiveButtonClick(position: Int) {
                val chat = ZopimChat.resume(activity)
                chat.endChat()
                chat.disconnect()
                activity!!.supportFragmentManager
                        .beginTransaction()
                        .remove(activity!!.supportFragmentManager.findFragmentByTag(ChatServiceBinder::class.java.name))
                        .commit()

                val transaction = childFragmentManager.beginTransaction()
                val zopimchatlogfragment= childFragmentManager.findFragmentByTag(ZopimChatLogFragment::class.java.name)
                if(zopimchatlogfragment !=null){
                    transaction.remove(zopimchatlogfragment)
                    transaction.commit()
                }

                childFragmentManager.popBackStack()

                (parentFragment as Profile).childFragmentManager.popBackStack()
            }
            override fun onNegativeButtonClick() {
                activity!!.supportFragmentManager
                        .beginTransaction()
                        .remove(activity!!.supportFragmentManager.findFragmentByTag(ChatServiceBinder::class.java.name))
                        .commit()

                childFragmentManager.popBackStack()

                (parentFragment as Profile).childFragmentManager.popBackStack()
            }
        })*/
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
