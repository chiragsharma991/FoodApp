package dk.eatmore.foodapp.service.fcm


import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.support.v4.content.ContextCompat
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.content.LocalBroadcastManager
import android.media.RingtoneManager
import android.app.NotificationChannel
import android.graphics.Color
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.Constants
import android.app.NotificationChannelGroup




class FirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.e(TAG, "onMessageReceived: " + remoteMessage!!.notification!!.body.toString())
      //  generateNotification(applicationContext,remoteMessage.notification!!.body.toString())
        //{vendor=vendor 1, offer_id=5a5f321bafcea80311f57699, type=offer, Title=TebeebBook, offer_title=Hospital, message=Added new offer, created_at=2018-01-11T04:03:55.311Z}
        //{id=5a8cfcd4148ee50ed0d470b6, body=HELLO ALL CITY ADMIN PUSH, type=message, title=Tabeeb Book}
        if (remoteMessage.data.isNotEmpty()) {
            Log.e(TAG, "Message data payload: " + remoteMessage.data)
          //  generateNotification(applicationContext, remoteMessage.data.get("message")!!, remoteMessage.data)

        }
    }
    //dxpDXzgeEus:APA91bFyqfIJLf3NWrH-9pFmM4a_GyXTNape5I4Rwd0KBPwo5WLN3XKv3lVm4o30Ngf2QO5K4_zKAhelGsw71iHjTAEuuBdJuwCg_90XfZNjOXhnoK3AliXFXXmpLUz67wqf8b0OEFEDxYmsBtzDwq3mrCq2reEGyw
    companion object {
        private val TAG = "MyFirebaseMsgService"

        @Synchronized
        fun generateNotification(context: Context, title: String) {
            //var mContext = LanguageWrapper.wrap(context, PreferenceUtil.getString(PreferenceUtil.LANGUAGE_SELECTION, Constants.ARABIC_LAN)!!)
            val resultIntent = Intent(context, HomeActivity::class.java)
            val resultPendingIntent = PendingIntent.getActivity(context,
                    0, resultIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT)

            val icon1 = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)

            val bigText = NotificationCompat.BigTextStyle()
            bigText.bigText("Big text title")
            bigText.setBigContentTitle("Big content title")
            //bigText.setSummaryText("");

            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = context.getString(R.string.default_notification_channel_id)
            val notification: Notification

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(channelId, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT)

                // Configure the notification channel.
                notificationChannel.description = "Channel description"
                notificationChannel.lightColor = Color.RED
                notificationChannel.enableLights(true)
                notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                notificationChannel.enableVibration(true)
                mNotificationManager.createNotificationChannel(notificationChannel)


                val groupId = "some_group_id"
                val groupName = "Some Group"
           //     mNotificationManager.createNotificationChannelGroup(NotificationChannelGroup(groupId, groupName))

            }
//  dYSw2u_Vcvw:APA91bEEctPxdTsqkRlMMPf-GXiKlub5VHaVngHEHnTGZ9s-RO95jamzAyykhXGJRX5mA7Z0wDVI_qBqWGVNlkDprcZw2-VxoXQA7Wwjddy3ZunOXsLTLW8ws6NXwi9-bT1GZluN5ansYO_OSVbMNUu4I70o1JlXwA
            notification = NotificationCompat.Builder(context, channelId)
                    .setVibrate(longArrayOf(0, 100, 100, 100, 100, 100))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setCategory(Notification.CATEGORY_PROMO)
                    .setContentTitle("Content title")
                    .setContentText(title)
                    .setSmallIcon(getNotificationIcon())
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setLargeIcon(icon1)
                    .setAutoCancel(true)
                    .setStyle(bigText)
                    .setContentIntent(resultPendingIntent)
                    .setPriority(if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationManager.IMPORTANCE_HIGH else Notification.PRIORITY_HIGH)
                    .build()

            mNotificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }


        private fun getNotificationIcon(): Int {
            val whiteIcon = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
            return if (whiteIcon) R.mipmap.ic_launcher else R.mipmap.ic_launcher
        }
    }
}