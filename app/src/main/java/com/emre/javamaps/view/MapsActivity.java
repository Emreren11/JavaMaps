package com.emre.javamaps.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.emre.javamaps.R;
import com.emre.javamaps.model.Place;
import com.emre.javamaps.roomDB.PlaceDao;
import com.emre.javamaps.roomDB.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.emre.javamaps.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLaucher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    PlaceDatabase db;
    PlaceDao placeDao;
    double selectedLatitude;
    double selectedLongitude;
    Place selectedPlace;
    private CompositeDisposable compositeDisposable = new CompositeDisposable(); // kullan - at

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sharedPreferences = MapsActivity.this.getSharedPreferences("com.emre.javamaps", MODE_PRIVATE);
        info = false;
        registerLauncher();

        db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places")/*.allowMainThreadQueries()*/.build();
        //allowMainThreadQueries - ufak veriler olduğunda kullanabiliriz ama önerilmez.
        placeDao = db.placeDao();

        selectedLongitude = 0.0;
        selectedLatitude = 0.0;
        binding.saveBtn.setEnabled(false);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String intentİnfo = intent.getStringExtra("info");

        if (intentİnfo.equals("new")) {
            binding.saveBtn.setVisibility(View.VISIBLE);
            binding.deleteBtn.setVisibility(View.GONE);

            //LocationManager //Sistemin konum servislerini kullanmaya olanak sağlar
            //(LocationManager) yapmazsak object olarak kalır ancak bize locationmanager lazım
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) { // Değişen konumu verir

                    info = sharedPreferences.getBoolean("info", false);

                    if (!info) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("info", true).apply();
                    }

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.getRoot(), "Permission Needed For Maps", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // İzin isteme kısmı
                            permissionLaucher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                } else {
                    //İzin isteme kısmı
                    permissionLaucher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }

                mMap.setMyLocationEnabled(true);
            }
        } else { // Yeni ekleme yapılmıyosa
            mMap.clear();
            selectedPlace = (Place) intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(selectedPlace.latitude, selectedPlace.longitute);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

            binding.placeNameText.setText(selectedPlace.name);
            binding.saveBtn.setVisibility(View.GONE);
            binding.deleteBtn.setVisibility(View.VISIBLE);
        }




        //Latitude, longitude - enlem, boylam
/*
        LatLng eiffel = new LatLng(48.8559713, 2.2930037);
        mMap.addMarker(new MarkerOptions().position(eiffel).title("Eiffel Tower")); // Başlangıçta işaretlenecek yeri belirleme
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(eiffel)); // Başlangıçta ekranı hangi konuma getireceğini belirleme
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel, 15)); // Başlangıçta ekranı hangi konuma getireceğini belirlerken yaklaştırma oranını da belirlenebilir
*/
    }

    private void registerLauncher() {
        permissionLaucher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    // İzin verildi
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null) {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                        }
                    }

                } else {
                    //İzin verilmedi
                    Toast.makeText(MapsActivity.this, "Permission Needed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveBtn.setEnabled(true);

    }

    public void save(View view) {

        Place place = new Place(binding.placeNameText.getText().toString(), selectedLatitude, selectedLongitude);

        // placeDao.insert(place);

        compositeDisposable.add(placeDao.insert(place).
                subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())//arkaplanda işlemi yap mainde göster
                .subscribe(MapsActivity.this::handleResponse) // referans verip metodu çalıştır
        );
    }

    private void handleResponse() { // gelen cevabı ele alma
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // tüm aktiviteleri kapatır
        startActivity(intent);
    }

    public void delete(View view) {

        compositeDisposable.add(placeDao.delete(selectedPlace).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(MapsActivity.this::handleResponse));


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear(); // çekilen verileri siler
    }
}

