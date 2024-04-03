package jr.project.reactsafe.parent;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
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

import jr.project.reactsafe.R;
import jr.project.reactsafe.SplashScreenActivity;
import jr.project.reactsafe.databinding.ActivityParentMainBinding;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.model.RecentModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.sensor.AccidentDetectionService;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.user.UserMainActivity;
import jr.project.reactsafe.user.UserPreferenceHelper;
import jr.project.reactsafe.user.UserSettingsActivity;

public class ParentMainActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityParentMainBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    GoogleMap mMap;
    Intent intent;
    LatLng cLoc = new LatLng(37.0902, 95.7129);
    ParentPreferenceHelper mPref;
    public ArrayList<UserModel> model;

    long updatedTs;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParentMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseHelper.getBlocked(val -> { if (val){ Extras.transferToBlocked(getApplicationContext()); }});

        mPref = new ParentPreferenceHelper(ParentMainActivity.this);
        intent = new Intent(ParentMainActivity.this, ParentForegroundService.class);

        if (mPref.getIsOnAccident()!=null){
            startActivity(new Intent(this,ParentAccidentProceedings.class));
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        dbRef.child("pairedBy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    stopService(intent);
                    mPref.setPairedDevice(null);
                    startActivity(new Intent(ParentMainActivity.this, SplashScreenActivity.class));
                    finishAffinity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        Glide.with(ParentMainActivity.this)
                .load(new UserPreferenceHelper(this).getProfileImage())
                .placeholder(R.drawable.avatar)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.myProfImg);

        binding.myProfImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ParentMainActivity.this, UserSettingsActivity.class));
            }
        });


        binding.start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(intent);
                }else {
                    stopService(intent);
                }
            }
        });

        binding.start.setChecked(isMyServiceRunning(ParentForegroundService.class));


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ParentMainActivity.this);

        if (status != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(ParentMainActivity.this, status, 0).show();
        }

        model = mPref.getPairedDeviceDetails();

        binding.pairedOn.setText("connected on "+Extras.getStandardFormDateFromTimeStamp(model.get(0).getPairedOn()));





//        FirebaseHelper.getPresence(model.get(0).getUid(), value -> {
//            if (value.equals("1")){
//                binding.pairedDeviceUserName.setText(model.get(0).getName()+"'s Device Online");
//            }else {
//                binding.pairedDeviceUserName.setText(model.get(0).getName()+"'s Device Offline");
//            }
//        });

        FirebaseHelper.getPairedUser(model1 -> {
            if (model1!=null && model1.getUid()!=null && !model1.getUid().equals(" ")){
                model.clear();
                model.add(model1);
                mPref.setPairedDevice(new Gson().toJson(model));
            }
        });

        FirebaseHelper.GetPairedUserLocation(model.get(0).getUid(), (lat, lng) -> {
            if (lat == null || lng == null)
                return;
            mMap.clear();
            updatedTs = Extras.getTimestamp();
            binding.lastUpdated.setText(Extras.getStandardFormDateFromTimeStamp(updatedTs+"")
                    +" at "+Extras.getTimeFromTimeStamp(updatedTs+""));
            LatLng myLoc = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
            mMap.addMarker(new MarkerOptions()
                    .position(myLoc)
                    .title("You")
                    .icon(Extras.bitmapFromVector(getApplicationContext(),R.drawable.marker_map_icon)));
            mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(myLoc,15));
        });

        try (DatabaseHelper db = new DatabaseHelper(this)){
            ArrayList<RecentModel> models = db.readRecentFalls();
            RecentRecyclerView adapter = new RecentRecyclerView(models);
            binding.rv.setLayoutManager(new LinearLayoutManager(this));
            binding.rv.setAdapter(adapter);

            if (!models.isEmpty()){
                binding.emptyRv.setVisibility(View.GONE);
                binding.rv.setVisibility(View.VISIBLE);
            }else {
                binding.emptyRv.setVisibility(View.VISIBLE);
                binding.rv.setVisibility(View.GONE);
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null){
            try {
                Glide.with(ParentMainActivity.this)
                        .load(new UserPreferenceHelper(this).getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(binding.myProfImg);
            }catch (Exception e){
                e.printStackTrace();
            }
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





    public class RecentRecyclerView extends RecyclerView.Adapter<RecentRecyclerView.MyViewHolder>{
        ArrayList<RecentModel> models;
        public RecentRecyclerView(ArrayList<RecentModel> models){
            this.models = models;
        }

        @NonNull
        @Override
        public RecentRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accident_alert_on_rv, parent, false);
            return new MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            RecentModel model = models.get(position);


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
            //Toast.makeText(ParentMainActivity.this, ""+models.size(), Toast.LENGTH_SHORT).show();
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