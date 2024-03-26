package jr.project.reactsafe.extras.auth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import jr.project.reactsafe.R;
import jr.project.reactsafe.SplashScreenActivity;
import jr.project.reactsafe.databinding.ActivityRegisterEntityBinding;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;

public class RegisterEntityActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityRegisterEntityBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    int title = 1;
    GoogleMap mMap;
    LocationRequest locationRequest;
    ProgressDialog progressDialog;
    LatLng cLoc = new LatLng(37.0902, 95.7129);
    LatLng myLoc;
    UserModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterEntityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(1000/2)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(RegisterEntityActivity.this);

        if (status != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(RegisterEntityActivity.this, status, 0).show();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableLocationSettings();
        }else {
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        binding.getLoc.setOnClickListener(v -> {
            getMyCurrentLoc();
        });


        binding.ambulance.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                title = 1;
                binding.police.setChecked(false);
                binding.hospital.setChecked(false);
            }
        });

        binding.police.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                title = 2;
                binding.ambulance.setChecked(false);
                binding.hospital.setChecked(false);
            }
        });

        binding.hospital.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                title = 3;
                binding.police.setChecked(false);
                binding.ambulance.setChecked(false);
            }
        });


        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateMyForm();
            }
        });

    }

    void ValidateMyForm(){

        String name = binding.etName.getText().toString();
        String email = binding.etEmail.getText().toString();
        String pass = binding.etPassword.getText().toString();
        String phone = binding.etPhone.getText().toString();

        if (name.isEmpty()){
            Toast.makeText(this, "Please enter a valid name.", Toast.LENGTH_SHORT).show();
            return;
        }else if (email.isEmpty()){
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
            }
            return;
        }else if (pass.isEmpty() || pass.length() < 6){
            Toast.makeText(this, "Password should be greater than six characters.", Toast.LENGTH_SHORT).show();
            return;
        }else if (phone.isEmpty() || phone.length() < 10){
            Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
            return;
        }else if (cLoc == myLoc){
            Toast.makeText(this, "Please select your present location.", Toast.LENGTH_SHORT).show();
            return;
        }
        showPleaseWaitDialog("Please wait...");
        model = new UserModel();
        model.setName(name);
        model.setEmail(email);
        model.setPhone(phone);
        model.setLat(String.valueOf(myLoc.latitude));
        model.setLng(String.valueOf(myLoc.longitude));
        if (title == 1){
            model.setTitle("ambulance");
        }else if (title == 2){
            model.setTitle("police");
        }else {
            model.setTitle("hospital");
        }

        signInWIthFirebase(email,pass);

    }

    void signInWIthFirebase(String email, String password){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    dismissPleaseWaitDialog();
                    if (task.isSuccessful()) {
                        Log.d("ReactSafeFirebaseAuth", "createUserWithEmail:success");
                        model.setUid(FirebaseAuth.getInstance().getUid());
                        FirebaseHelper.InsertUser(model);
                        if (title == 1){
                            FirebaseHelper.InsertAmbulance(model);
                            new SharedPreference(RegisterEntityActivity.this).setUserTypeInPref("ambulance");
                        }else if (title == 2){
                            FirebaseHelper.InsertPolice(model);
                            new SharedPreference(RegisterEntityActivity.this).setUserTypeInPref("police");
                        }else {
                            FirebaseHelper.InsertHospital(model);
                            new SharedPreference(RegisterEntityActivity.this).setUserTypeInPref("hospital");
                        }
                        startActivity(new Intent(RegisterEntityActivity.this, SplashScreenActivity.class));
                    } else {
                        Log.w("ReactSafeFirebaseAuth","createUserWithEmail:failure", task.getException());
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.addMarker(new MarkerOptions()
                .position(cLoc)
                .title("Marker in Sydney")
                .icon(Extras.bitmapFromVector(getApplicationContext(),R.drawable.marker_map_icon)));
        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(cLoc,1));

    }

    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            enableLocationSettings();
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            enableLocationSettings();
                        } else {
                            Toast.makeText(this, "Please enable location to continue.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @SuppressLint("MissingPermission")
    protected void getMyCurrentLoc(){
        showPleaseWaitDialog("Fetching your location...");
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();

                dismissPleaseWaitDialog();

                if (location != null) {
                    myLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(myLoc)
                            .title("You")
                            .icon(Extras.bitmapFromVector(getApplicationContext(),R.drawable.marker_map_icon)));
                    mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(myLoc,15));
                }else {
                    Toast.makeText(RegisterEntityActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        }, Looper.getMainLooper());
    }

    @SuppressLint("MissingPermission")
    protected void enableLocationSettings() {

        final LocationManager manager = (LocationManager) RegisterEntityActivity.this.getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            getMyCurrentLoc();
            return;
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(this, (LocationSettingsResponse response) -> {
                    getMyCurrentLoc();
                })
                .addOnFailureListener(this, ex -> {
                    if (ex instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) ex;
                            resolvable.startResolutionForResult(RegisterEntityActivity.this, 111);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111){
            if (resultCode == Activity.RESULT_OK){
                getMyCurrentLoc();
            }
        }
    }

    private void showPleaseWaitDialog(String msg) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissPleaseWaitDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}