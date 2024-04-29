package jr.project.reactsafe.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import jr.project.reactsafe.ApplicationController;
import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityUserMainBinding;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.RecentModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.sensor.AccidentDetectionService;
import jr.project.reactsafe.extras.util.Extras;

public class UserMainActivity extends AppCompatActivity implements OnMapReadyCallback {


    ActivityUserMainBinding binding;
    SharedPreference mPref;
    Intent intent;

    private FusedLocationProviderClient fusedLocationClient;
    int title = 1;
    GoogleMap mMap;
    LocationRequest locationRequest;
    ProgressDialog progressDialog;
    LatLng cLoc = new LatLng(37.0902, 95.7129);
    LatLng myLoc;
    double lat,lng;
    ArrayList<UserModel> mo;
    RecentRecyclerView adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseHelper.getBlocked(val -> { if (val){ Extras.transferToBlocked(getApplicationContext()); }});

        mPref  = new SharedPreference(UserMainActivity.this);
        intent = new Intent(UserMainActivity.this, AccidentDetectionService.class);

        setUi();

        lat = Double.parseDouble(mPref.getString("lastLatitude",37.0902+""));
        lng = Double.parseDouble(mPref.getString("lastLongitude",95.7129+""));

        cLoc = new LatLng(lat,lng);

        long mapLastUpdatedTs = mPref.getLong("lastLocationUpdate",Extras.getTimestamp());
        String mapLastUpdatedString = "updated as on "+
                Extras.getStandardFormDateFromTimeStamp(mapLastUpdatedTs+"")+" "+
                Extras.getTimeFromTimeStamp(mapLastUpdatedTs+"");

        binding.mapUpdatedOnTxt.setText(mapLastUpdatedString);

        binding.on.setOnClickListener(v -> setRunOrNot());

        try{
        Glide.with(UserMainActivity.this)
                .load(new UserPreferenceHelper(UserMainActivity.this))
                .placeholder(R.drawable.avatar)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.myProfImg);
        }catch (Exception e){
            e.printStackTrace();
        }

        binding.myProfImg.setOnClickListener(v -> startActivity(new Intent(UserMainActivity.this,UserSettingsActivity.class)));

        binding.pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserMainActivity.this, PairParentActivity.class));
            }
        });

        if (getIntent()!=null && getIntent().getStringExtra("fromIntent")!=null){
            String fr = getIntent().getStringExtra("fromIntent");

            switch (fr){
                case "AccidentAlertActivity.class":
                    if (intent!=null){
                        startService(intent);
                        mPref.putBoolean("startedReactLooks",true);
                        int cardColor  = ContextCompat.getColor(UserMainActivity.this,R.color.green_light);
                        int visibility = View.GONE;
                        String proTxt  = "Protected";
                        binding.alertSymbol.setVisibility(visibility);
                        binding.onCard.setCardBackgroundColor(cardColor);
                        binding.protTxt.setText(proTxt);
                    }

            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(1000/2)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(UserMainActivity.this);

        if (status != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(UserMainActivity.this, status, 0).show();
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

        getMyCurrentLoc();

        try (DatabaseHelper db = new DatabaseHelper(this)){
            ArrayList<RecentModel> models = db.readRecentFalls();
            adapter = new RecentRecyclerView(models);
            binding.rv.setLayoutManager(new LinearLayoutManager(this));
            binding.rv.setAdapter(adapter);

            if (!models.isEmpty()){
                binding.emptyRv.setVisibility(View.GONE);
                binding.rv.setVisibility(View.VISIBLE);
            }else {
                binding.emptyRv.setVisibility(View.VISIBLE);
                binding.rv.setVisibility(View.GONE);
            }

            for (int i = 0 ; i < models.size() ; i++){
                RecentModel model = models.get(i);
                FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                        .child("alerts").child(models.get(i).getTimestamp()).child("status")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String val = snapshot.getValue(String.class);
                            db.updateAlert(model.getTimestamp(),val);
                            model.setStatus(val);
                            adapter.setModel(models);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

        }

        setProfileDetails();
        setPairedDevice();

        final LocationManager manager = (LocationManager) UserMainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            EnableLocationOrExit();
        }


        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users")
                        .child(FirebaseAuth.getInstance().getUid());
                dbRef.child("pairedBy").removeValue();
                dbRef.child("pairedOn").removeValue();
                if (mo!=null && !mo.isEmpty()){
                    DatabaseReference dbRef2 = FirebaseDatabase.getInstance().getReference().child("users")
                            .child(mo.get(0).getUid());
                    dbRef2.child("pairedBy").removeValue();
                    dbRef2.child("pairedOn").removeValue();
                }
                //
                new UserPreferenceHelper(UserMainActivity.this)
                        .setPairedDeviceDetails(null);

                binding.pairBtn.setVisibility(View.VISIBLE);
                binding.linkedDeviceHolder.setVisibility(View.GONE);
            }
        });

    }

    void setPairedDevice(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        dbRef.child("pairedBy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    new UserPreferenceHelper(UserMainActivity.this).setPairedDeviceDetails(null);
                    setUi();
                }else {
                    FirebaseHelper.getUser(snapshot.getValue(String.class), new FirebaseHelper.OnReceivedUser() {
                        @Override
                        public void getReceiver(UserModel model) {
                            if (model!=null) {
                                ArrayList<UserModel> m = new ArrayList<>();
                                m.add(model);
                                new UserPreferenceHelper(UserMainActivity.this)
                                        .setPairedDeviceDetails(new Gson().toJson(m));
                                setUi();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void setProfileDetails(){
        FirebaseHelper.getUser(FirebaseAuth.getInstance().getUid(), model -> {
            new UserPreferenceHelper(UserMainActivity.this).setProfileEmail(model.getEmail());
            new UserPreferenceHelper(UserMainActivity.this).setProfileNumber(model.getPhone());
            new UserPreferenceHelper(UserMainActivity.this).setProfileImage(model.getProfileImage());
            new UserPreferenceHelper(UserMainActivity.this).setProfileName(model.getName());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUi();
        if (binding!=null)
            try{
            Glide.with(UserMainActivity.this)
                .load(new UserPreferenceHelper(UserMainActivity.this).getProfileImage())
                .placeholder(R.drawable.avatar)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.myProfImg);
            }catch (Exception e){
                e.printStackTrace();
            }

        //Toast.makeText(this, ""+isMyServiceRunning(AccidentDetectionService.class), Toast.LENGTH_SHORT).show();
    }

    void setRunOrNot(){
        boolean isRun = mPref.getBoolean("startedReactLooks",false);
        int cardColor,visibility;
        String proTxt;

        if (isRun){
            stopService(intent);
            mPref.putBoolean("startedReactLooks",false);
            cardColor  = ContextCompat.getColor(UserMainActivity.this,R.color.red_light);
            visibility = View.VISIBLE;
            proTxt     = "Un Protected";
            ApplicationController.releaseMediaPlayer();
        }else {
            startService(intent);
            mPref.putBoolean("startedReactLooks",true);
            cardColor  = ContextCompat.getColor(UserMainActivity.this,R.color.green_light);
            visibility = View.GONE;
            proTxt     = "Protected";
        }

        binding.alertSymbol.setVisibility(visibility);
        binding.onCard.setCardBackgroundColor(cardColor);
        binding.protTxt.setText(proTxt);

    }

    @SuppressLint("SetTextI18n")
    void setUi(){
        try {
            if (binding == null) {
                return;
            }
            if (mPref == null) {
                mPref = new SharedPreference(UserMainActivity.this);
            }
            boolean isRun = mPref.getBoolean("startedReactLooks", false);
            int cardColor, visibility;
            String proTxt;

            if (!isRun) {
                cardColor = ContextCompat.getColor(UserMainActivity.this, R.color.red_light);
                visibility = View.VISIBLE;
                proTxt = "Un Protected";
            } else {
                startService(intent);
                cardColor = ContextCompat.getColor(UserMainActivity.this, R.color.green_light);
                visibility = View.GONE;
                proTxt = "Protected";
            }

            binding.alertSymbol.setVisibility(visibility);
            binding.onCard.setCardBackgroundColor(cardColor);
            binding.protTxt.setText(proTxt);

            mo = new UserPreferenceHelper(UserMainActivity.this)
                    .getPairedDeviceDetails();
            if (mo != null && !mo.isEmpty()) {
                UserModel model = mo.get(0);
                binding.pairBtn.setVisibility(View.GONE);
                binding.linkedDeviceHolder.setVisibility(View.VISIBLE);
                try {
                    Glide.with(UserMainActivity.this)
                            .load(model.getProfileImage())
                            .placeholder(R.drawable.avatar)
                            .into(binding.conIv);
                }catch (Exception e){
                    e.printStackTrace();
                }

                binding.conName.setText(model.getName());
                binding.conDate.setText("connected on " + Extras.getStandardFormDateFromTimeStamp(model.getPairedOn()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.addMarker(new MarkerOptions()
                .position(cLoc)
                .title("You")
                .icon(Extras.bitmapFromVector(getApplicationContext(),R.drawable.marker_map_icon)));
        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(cLoc,15));

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

    Runnable timerRunnable;
    Handler timerHandler = new Handler();
    private long lastUpdateTime = 0;
    @SuppressLint("MissingPermission")
    protected void getMyCurrentLoc(){
        //showPleaseWaitDialog("Fetching your location...");
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();

                //dismissPleaseWaitDialog();
                //try {
                //    progressDialog.dismiss();
               // }catch (Exception e){
                //    e.printStackTrace();
                //}

                if (location != null) {
                    myLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    mPref.putString("lastLatitude",location.getLatitude()+"");
                    mPref.putString("lastLongitude",location.getLongitude()+"");
                    mPref.putLong("lastLocationUpdate",Extras.getTimestamp());

                    updateLocation(location);

//                    timerRunnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            runOnUiThread(() -> {
//                                mMap.clear();
//                                mMap.addMarker(new MarkerOptions()
//                                        .position(myLoc)
//                                        .title("You")
//                                        .icon(Extras.bitmapFromVector(getApplicationContext(),R.drawable.marker_map_icon)));
//                                mMap.moveCamera(
//                                        CameraUpdateFactory.newLatLngZoom(myLoc,15));
//                            });
//                            Log.e("MainLocationLooper", new Gson().toJson(myLoc));
//                            FirebaseHelper.InsertLocation(location.getLatitude()+"",location.getLongitude()+"");
//                            timerHandler.postDelayed(timerRunnable, 60000);
//                        }
//                    };
//
//
//                    timerHandler.post(timerRunnable);

                }else {
                    Toast.makeText(UserMainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        }, Looper.getMainLooper());
    }


    public void startUpdatingLocation(Location location) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateLocation(location);
            }
        }, 0, 60000);
    }

    private void updateLocation(Location location) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastUpdateTime;
        if (lastUpdateTime == 0 || elapsedTime >= 60000) { // 60 seconds
            sendLocationUpdates(location);
            lastUpdateTime = currentTime;
        }
    }

    private void sendLocationUpdates(Location location) {
        runOnUiThread(() -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(myLoc)
                    .title("You")
                    .icon(Extras.bitmapFromVector(getApplicationContext(),R.drawable.marker_map_icon)));
            mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(myLoc,15));

            long mapLastUpdatedTs = Extras.getTimestamp();
            String mapLastUpdatedString = "updated as on "+
                    Extras.getStandardFormDateFromTimeStamp(mapLastUpdatedTs+"")+" "+
                    Extras.getTimeFromTimeStamp(mapLastUpdatedTs+"");

            binding.mapUpdatedOnTxt.setText(mapLastUpdatedString);

        });
        Log.e("MainLocationLooper", new Gson().toJson(myLoc));
        FirebaseHelper.InsertLocation(location.getLatitude()+"",location.getLongitude()+"");
    }



    @SuppressLint("MissingPermission")
    protected void enableLocationSettings() {

        final LocationManager manager = (LocationManager) UserMainActivity.this.getSystemService(Context.LOCATION_SERVICE);

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
                    Toast.makeText(this, "React Safe will not work without location!", Toast.LENGTH_SHORT).show();
                    if (ex instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) ex;
                            resolvable.startResolutionForResult(UserMainActivity.this, 111);
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

    public void EnableLocationOrExit(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Setting Alert Dialog Title
        alertDialogBuilder.setTitle("Enable Location!");
        // Icon Of Alert Dialog
        alertDialogBuilder.setIcon(R.drawable.react_safe_l);
        // Setting Alert Dialog Message
        alertDialogBuilder.setMessage("Please enable location to continue using React Safe.");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                enableLocationSettings();
            }
        });

        alertDialogBuilder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public class RecentRecyclerView extends RecyclerView.Adapter<RecentRecyclerView.MyViewHolder>{
        ArrayList<RecentModel> models;
        public RecentRecyclerView(ArrayList<RecentModel> models){
            this.models = models;
        }

        public void setModel(ArrayList<RecentModel> models){
            this.models = models;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accident_alert_on_rv, parent, false);
            return new MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            RecentModel model = models.get(position);

            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(UserMainActivity.this, ""+model.getTimestamp(), Toast.LENGTH_SHORT).show();
                }
            });
            holder.title.setText(model.getLocation());


            String text = null;
            if (Objects.equals(model.getStatus(),"1")){
                text = "Accident detected at "+Extras.getStandardFormDateFromTimeStamp(model.getTimestamp())
                        +" on "+Extras.getTimeFromTimeStamp(model.getTimestamp());
            }else if (Objects.equals(model.getStatus(),"2")){
                text = "Cancelled by you at "+Extras.getStandardFormDateFromTimeStamp(model.getTimestamp())
                        +" on "+Extras.getTimeFromTimeStamp(model.getTimestamp());
            }else if (Objects.equals(model.getStatus(),"3")){
                text = "Cancelled by parent at "+Extras.getStandardFormDateFromTimeStamp(model.getTimestamp())
                        +" on "+Extras.getTimeFromTimeStamp(model.getTimestamp());
            }else {
                text = "Cancelled at "+Extras.getStandardFormDateFromTimeStamp(model.getTimestamp())
                        +" on "+Extras.getTimeFromTimeStamp(model.getTimestamp());
            }

            holder.desc.setText(text);

        }


        @Override
        public int getItemCount() {
            return models.size();
        }


        public class MyViewHolder extends RecyclerView.ViewHolder{
            TextView title,desc;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                desc  = itemView.findViewById(R.id.desc);
            }
        }
    }
}