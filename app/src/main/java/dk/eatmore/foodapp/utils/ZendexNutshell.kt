package dk.eatmore.foodapp.utils

import android.app.Application

import com.zendesk.logger.Logger
import com.zendesk.util.StringUtils

import zendesk.core.AnonymousIdentity
import zendesk.core.JwtIdentity
import zendesk.core.Zendesk
import zendesk.support.Support

class ZendexNutshell : Application() {

    companion object {

        private val SUBDOMAIN_URL = "https://xyz5070.zendesk.com"
        private val APPLICATION_ID = "5607f30269e67f046f086eae038d6c1abf60d0e6490a03ae"
        private val OAUTH_CLIENT_ID = "mobile_sdk_client_e5c9b367c7d7adf62d77"

        internal var isMissingCredentials = false
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Enable logging
        Logger.setLoggable(true)

        if (StringUtils.isEmpty(SUBDOMAIN_URL)
                || StringUtils.isEmpty(APPLICATION_ID)
                || StringUtils.isEmpty(OAUTH_CLIENT_ID)) {
            isMissingCredentials = true
            return
        }

        /**
         * Initialize the SDK with your Zendesk subdomain, mobile SDK app ID, and client ID.
         *
         * Get these details from your Zendesk dashboard: Admin -> Channels -> MobileSDK.
         */
        Zendesk.INSTANCE.init(this,
                SUBDOMAIN_URL,
                APPLICATION_ID,
                OAUTH_CLIENT_ID)

        /**
         * Set an identity (authentication).
         *
         * Set either Anonymous or JWT identity, as below:
         */

        // a). Anonymous (All fields are optional)
        Zendesk.INSTANCE.setIdentity(
                AnonymousIdentity.Builder()
                        .withNameIdentifier("chari-name")
                        .withEmailIdentifier("charisharma@gmail.com")
                        .build()
        )


        // b). JWT (Must be initialized with your JWT identifier)
        // Zendesk.INSTANCE.setIdentity(new JwtIdentity("jfdIgU1QQoGjHZBuPMqj5kEJY7gnbgIM4az9pjH3jz2S7hgX"));

        Support.INSTANCE.init(Zendesk.INSTANCE)
    }


}
