package dk.eatmore.foodapp.utils

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task

/**
 * Created by Mohit on 15-Feb-18.
 */

class LocationUpdate(private val context: Activity, val onLocationInteraction: OnLocationInteraction) {

    /**
     * Provides access to the Fused LocationUpdate Provider API.
     */
    private val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

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
     * Callback for LocationUpdate events.
     */
    private var mLocationCallback: LocationCallback? = null

    /**
     * Represents a geographical location.
     */
    private var mCurrentLocation: Location? = null

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    var mRequestingLocationUpdates: Boolean? = false

    init {
        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()

        if ((!mRequestingLocationUpdates!!) && checkPermissions()) {
            mRequestingLocationUpdates = true
            startLocationUpdates()
        } else if (!checkPermissions()) {
            this.onLocationInteraction.onReqPermission()
        }
    }

    private fun updateLocationUI() {
        Log.e("updateLocationUI--","")
        if (mCurrentLocation != null) {
            onLocationInteraction.onLocationUpdate(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
        }
    }

    interface OnLocationInteraction {
        fun onLocationUpdate(lat: Double, lng: Double)
        fun onReqPermission()
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Creates a callback for receiving location events.
     */
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


    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()

        //mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        //mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS

        /**
         * Set 1KM
         */
        //mLocationRequest!!.smallestDisplacement = 1000F

        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    fun chnageIs(isChange : Boolean){
        mRequestingLocationUpdates = isChange
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


                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback!!, Looper.myLooper())

                    updateLocationUI()
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
                            mRequestingLocationUpdates = false
                        }
                    }

                    updateLocationUI()
                }
    }

    fun stopLocationUpdates() {
        if (!mRequestingLocationUpdates!!) {
            return
        }

        mRequestingLocationUpdates = false
        mFusedLocationClient.removeLocationUpdates(mLocationCallback!!)
                .addOnCompleteListener(context) { mRequestingLocationUpdates = false }
    }

    companion object {

        private val TAG = "LocationUpdate"

        /**
         * Constant used in the location settings dialog.
         */
        val REQUEST_CHECK_SETTINGS = 0x1

        private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 60000
        private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

}