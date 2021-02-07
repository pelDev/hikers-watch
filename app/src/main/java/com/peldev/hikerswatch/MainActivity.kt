package com.peldev.hikerswatch

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import java.util.*


class MainActivity : AppCompatActivity() {
//
//    private val locManager by lazy {
//        this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var locListener: LocationListener

    private lateinit var latTextView: TextView
    private lateinit var lonTextView: TextView
    private lateinit var altTextView: TextView
    private lateinit var accTextView: TextView
    private lateinit var addTextView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        latTextView = findViewById(R.id.latTextView)
        lonTextView = findViewById(R.id.lonTextView)
        altTextView = findViewById(R.id.altTextView)
        accTextView = findViewById(R.id.accTextView)
        addTextView = findViewById(R.id.addTextView)


        fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this)


//        locListener  = object : LocationListener {
//            override fun onLocationChanged(location: Location) {
//                Log.d("locationInfo", location.latitude.toString())
//                updateLocationInfo(location)
//            }
//
//            override fun onProviderEnabled(provider: String) {}
//            override fun onProviderDisabled(provider: String) {}
//            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
//        }

        startListening()
        checkForPermissions()

    }

    @SuppressLint("ObsoleteSdkInt")
    private fun checkForPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            startLocationUpdates()
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                )
            } else {
                startLocationUpdates()

                fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null)
                                updateLocationInfo(location)
                        }

//                val location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//
//                location?.let {
//                    updateLocationInfo(it)
//                }
            }
        }
    }

    private fun startListening() {
        fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.smallestDisplacement = 0f
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    val location: Location = locationResult.lastLocation
                    updateLocationInfo(location)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
            )
    }

    override fun onPause() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onPause()
    }

//    private fun startListening() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED)
////            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, locListener)
//    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    private fun updateLocationInfo(loc: Location) {
        Log.i("LocationInfo", loc.toString())
        latTextView.text = "Latitude: ${loc.latitude}"
        lonTextView.text = "Longitude: ${loc.longitude}"
        altTextView.text = "Altitude: ${loc.altitude}"
        accTextView.text = "Accuracy: ${loc.accuracy}"

        val geocoder = Geocoder(applicationContext, Locale.getDefault())

        try {
            var address = "Address Not Found!"

            val listAddresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)

            if (!listAddresses.isNullOrEmpty()) {
                address = "Address: \n"
                if (listAddresses[0].subThoroughfare.isNotEmpty())
                    address += listAddresses[0].subThoroughfare + " "

                if (listAddresses[0].thoroughfare.isNotEmpty())
                    address += listAddresses[0].thoroughfare + "\n"

                if (listAddresses[0].locality.isNotEmpty())
                    address += listAddresses[0].locality + "\n"

                if (listAddresses[0].postalCode.isNotEmpty())
                    address += listAddresses[0].postalCode + "\n"

                if (listAddresses[0].countryName.isNotEmpty())
                    address += listAddresses[0].countryName + "\n"
            }

            addTextView.text = address
        } catch (e: Throwable) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

    }
}