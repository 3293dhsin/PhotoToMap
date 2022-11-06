package com.example.phototomap
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var dbHelper: DBHelper
    var currentImagePath: String? = null
    private var locationList = ArrayList<Location>()
    companion object {
        var IMAGE_REQUEST: Int = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = DBHelper(this)
        //dbHelper.deleteLocationList()
        locationList = dbHelper.getLocationList()
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            // once the map is ready, this callback will be executed
            googleMap = it
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) { }
            // enable the current location of your device
            googleMap.isMyLocationEnabled = true
            for (i in locationList) {
                var j = LatLng(i.latitude, i.longitude)
                googleMap.addMarker(MarkerOptions().position(j))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(j, 12f))
            }
            googleMap.setOnMarkerClickListener {
                var address = getAddress(it.position)
                locationList = dbHelper.getLocationList()
                for (i in locationList) {
                    if (it.position.longitude == i.longitude && it.position.latitude == i.latitude) {
                        openBottomSheet(address, i.image)
                    }
                }
                true
            }
        })
        var floatingactionbtn: FloatingActionButton = findViewById(R.id.floatingActionButton)
        floatingactionbtn.setOnClickListener {
            captureImage()
            getCurrentLocation()
        }
    }
    private fun openBottomSheet(address: String, image: String) {
        var bottomSheetFragment = BottomSheetFragment.newInstance(address, image)
        bottomSheetFragment.show(supportFragmentManager, "")
    }
    private fun captureImage() {
        var cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager)!=null) {
            var imageFile: File? = null
            try {
                imageFile = getImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (imageFile!=null) {
                var imageURI: Uri = FileProvider.getUriForFile(this,"com.example.android.fileprovider", imageFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                startActivityForResult(cameraIntent, IMAGE_REQUEST)
            }
        }
    }
    private fun getImageFile(): File {
        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageName: String = "jpg_" + timeStamp + "_"
        var storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var imageFile: File = File.createTempFile(imageName, ".jpg", storageDir)
        currentImagePath = imageFile.absolutePath
        return imageFile
    }
    private fun getCurrentLocation() {
        var locationRequest = com.google.android.gms.location.LocationRequest()
        locationRequest.setInterval(10000)
        locationRequest.setFastestInterval(3000)
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) { }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
            locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@MainActivity).removeLocationUpdates(this)
                    if(locationResult != null && locationResult.locations.size > 0) {
                        var latestLocationIndex: Int = locationResult.locations.size - 1
                        var latitude: Double = locationResult.locations.get(latestLocationIndex).latitude
                        var longitude: Double = locationResult.locations.get(latestLocationIndex).longitude
                        var mycoordinates = LatLng(latitude, longitude)
                        var address = getAddress(mycoordinates)
                        if (currentImagePath != null) {
                            dbHelper.insertLocation(Location(longitude, latitude, currentImagePath!!))
                            var j = LatLng(latitude, longitude)
                            googleMap.addMarker(MarkerOptions().position(j))
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(j, 12f))
                        }
                    }
                }
            },
            Looper.getMainLooper()
        )
    }
    private fun getAddress(mycoordinates: LatLng): String {
        var geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address> = geocoder.getFromLocation(mycoordinates.latitude, mycoordinates.longitude, 1)
        var address: String = addresses.get(0).getAddressLine(0)
        return address
    }
}