package dk.eatmore.foodapp.utils

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

/**
 * Created by Mohit on 15-Feb-18.
 */

class GetLastLocation(private val context: Activity, val onLocationInteraction: OnLocationInteraction) {

    /**
     * Provides access to the LocationUpdate Settings API.
     */
    private val mSettingsClient: SettingsClient = LocationServices.getSettingsClient(context)

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private var mLocationSettingsRequest: LocationSettingsRequest? = null

    /**
     * Represents a geographical location.
     */
    private var mCurrentLocation: Location? = null

    private var mLocationCallback: LocationCallback? = null

    private val UPDATE_INTERVAL = (2 * 1000).toLong()
    //10 secs
    private val FASTEST_INTERVAL: Long = 2000
    //2 sec
    private  var fusedLocationClient: FusedLocationProviderClient?=null




    interface OnLocationInteraction {
        fun onLocationUpdate(lat: Double, lng: Double)
        fun onReqPermission()
    }



    init {
        createLocationRequest()
        createLocationCallback()
        buildLocationSettingsRequest()
        startLocationUpdates()


        /*   if (checkPermissions()) {
               startLocationUpdates()
           } else if (!checkPermissions()) {
               this.onLocationInteraction.onReqPermission()
           }*/
    }

    private fun updateLocationUI() {
        if (mCurrentLocation != null) {
            onLocationInteraction.onLocationUpdate(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Creates a callback for receiving location events.
     */



    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    /*    mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)*/
    }

    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                Log.e("mLocationCallback--",""+locationResult!!.locations)
                mCurrentLocation = locationResult.lastLocation
                updateLocationUI()

            }
        }
    }


    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    fun startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(context) {
                    Log.e(TAG, "All location settings are satisfied.")

                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "Check permission")
                        return@addOnSuccessListener
                    }
                    fusedLocationClient!!.requestLocationUpdates(mLocationRequest,
                            mLocationCallback!!, Looper.myLooper())

                }
                .addOnFailureListener(context) { e ->
                    val statusCode = (e as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.e(TAG, "LocationUpdate settings are not satisfied. Attempting to upgrade " + "location settings ")
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(context, REQUEST_CHECK_SETTINGS)
                            } catch (sie: IntentSender.SendIntentException) {
                                Log.e(TAG, "PendingIntent unable to execute request.")
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "LocationUpdate settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                            Log.e(TAG, errorMessage)
                        }
                    }
                    updateLocationUI()
                }
    }

 /*   fun stopLocationUpdates() {
        if (!mRequestingLocationUpdates!!) {
            return
        }

        mRequestingLocationUpdates = false
        mFusedLocationClient.removeLocationUpdates(mLocationCallback!!)
                .addOnCompleteListener(context) { mRequestingLocationUpdates = false }
    }*/


    companion object {
        private val TAG = "GetLastLocation"
        /**
         * Constant used in the location settings dialog.
         */
        val REQUEST_CHECK_SETTINGS = 0x1
    }

}