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
import android.provider.Settings
import com.google.firebase.iid.FirebaseInstanceId


class FirebaseMessagingService : FirebaseMessagingService() {


    override fun onNewToken(refreshedToken: String?) {

        Log.e(TAG, "Refreshed token:--- " + refreshedToken!!)

        PreferenceUtil.putValue(PreferenceUtil.DEVICE_TOKEN,refreshedToken )
        PreferenceUtil.save()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.e(TAG, "onMessageReceived: " + remoteMessage!!.notification!!.body.toString())

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.e("TAG", "Message data payload: " + remoteMessage.data)

        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.e("TAG", "Message Notification Body: " + remoteMessage.notification!!.body!!)
        }


    }

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
                    .setPriority(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationManager.IMPORTANCE_HIGH else Notification.PRIORITY_HIGH)
                    .build()

            mNotificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }


        private fun getNotificationIcon(): Int {
            val whiteIcon = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
            return if (whiteIcon) R.mipmap.ic_launcher else R.mipmap.ic_launcher
        }
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Content title")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}