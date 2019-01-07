package dk.eatmore.foodapp.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import dk.eatmore.foodapp.R.drawable.location
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

import dk.eatmore.foodapp.R


abstract class  LocationFinder : CommanAPI(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener  {

    abstract fun locationtracking_success(address : Address)
    abstract fun locationtracking_failed()
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()
 //10 secs
    private val FASTEST_INTERVAL: Long = 2000
 //2 sec

    private var locationManager: LocationManager? = null
    private val TAG= "LocationFinder"


    private val isLocationEnabled: Boolean
        get() {
            locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loge(TAG,"initialise oncreate --")


        mGoogleApiClient = GoogleApiClient.Builder(context!!)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocation() //check whether location service is enable or not in your  phone
    }

    fun initialise(){
        loge(TAG,"initialise --")

    /*    mGoogleApiClient = GoogleApiClient.Builder(context!!)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocation() //check whether location service is enable or not in your  phone*/
    }




    override fun onConnected(p0: Bundle?) {
        Log.e(TAG, "onConnected--")

        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            locationtracking_failed()
            Toast.makeText(context!!, "Check permission", Toast.LENGTH_SHORT).show()

            return
        }

        startLocationUpdates()

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        if (mLocation == null) {
            startLocationUpdates()
        }
        if (mLocation != null) {

            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
            Toast.makeText(context!!, "Location not equal null"+mLocation!!.getLatitude(), Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(context!!, "Location is null ", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e(TAG, "Connection Suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e(TAG, "Connection failed. Error: " + p0.getErrorCode())
        locationtracking_failed()

    }

    override fun onLocationChanged(p0: Location?) {

        val msg = "Updated Location: " +
                java.lang.Double.toString(p0!!.latitude) + "," +
                java.lang.Double.toString(p0.longitude)
        Log.e(TAG,"Updated Location: "+msg)
        Toast.makeText(context!!, msg, Toast.LENGTH_SHORT).show()

        // You can now create a LatLng Object for use with maps
        val latLng = LatLng(p0.latitude, p0.longitude)

        val gcd =Geocoder(context!!)
        val addresses  :List<Address> = gcd.getFromLocation(p0.latitude,p0.longitude,10)

        for (address : Address in  addresses) {
            if(address.getLocality()!=null && address.getPostalCode()!=null){
                Log.e(TAG,address.getLocality())
                Log.e(TAG,address.getPostalCode())
                locationtracking_success(address)
            }
        }

    }

    protected fun startLocationUpdates() {
        Log.e("startLocationUpdates", "--->>>>")

        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            locationtracking_failed()
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this)
    }

    private fun checkLocation(): Boolean {
        if (!isLocationEnabled)
            showAlert()
        return isLocationEnabled
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(context!!)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt ->
                    locationtracking_failed()
                }
        dialog.show()
    }


    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

}
