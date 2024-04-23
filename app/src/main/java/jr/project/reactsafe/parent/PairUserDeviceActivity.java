package jr.project.reactsafe.parent;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import jr.project.reactsafe.R;
import jr.project.reactsafe.SplashScreenActivity;
import jr.project.reactsafe.ambulance.AmbulanceForegroundService;
import jr.project.reactsafe.databinding.ActivityPairUserDeviceBinding;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.sensor.AccidentDetectionService;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.hospital.HospitalForegroundService;
import jr.project.reactsafe.police.PoliceForegroundService;
import jr.project.reactsafe.user.PairParentActivity;
import jr.project.reactsafe.user.UserSettingsActivity;

public class PairUserDeviceActivity extends AppCompatActivity {

    ActivityPairUserDeviceBinding binding;
    ParentPreferenceHelper mPref;
    long newCode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPairUserDeviceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(v -> finish());

        FirebaseHelper.getBlocked(val -> { if (val){ Extras.transferToBlocked(getApplicationContext()); }});

        mPref = new ParentPreferenceHelper(this);

        Random rand = new Random();
        newCode = rand.nextInt(10000000);

        if (mPref.getLastGenCode() > 0){
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child("pairCode").child(mPref.getLastGenCode()+"").removeValue();
        }

        FirebaseHelper.InsertPairCode(newCode+"");
        mPref.setLastGenCode(newCode);

        binding.code.setText(newCode+"");

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        dbRef.child("pairedBy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    FirebaseHelper.getUser(snapshot.getValue(String.class), (model) -> {
                        ArrayList<UserModel> m = new ArrayList<>();
                        m.add(model);
                        mPref.setPairedDevice(new Gson().toJson(m));
                        startActivity(new Intent(PairUserDeviceActivity.this, ParentMainActivity.class));
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignOutDialog();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newCode > 0)
            FirebaseDatabase.getInstance().getReference()
                .child("users").child("pairCode").child(String.valueOf(newCode)).removeValue();

    }


    public void showSignOutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PairUserDeviceActivity.this);
        builder.setTitle("Sign Out?")
                .setMessage("Are you sure you want to sign out of React Safe?")
                .setPositiveButton("NO", (dialog, which) -> { })
                .setNegativeButton("YES", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    cancelNotification();
                    clearData();
                    clearServices();
                    startActivity(new Intent(PairUserDeviceActivity.this, SplashScreenActivity.class));
                    finishAffinity();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    void clearData(){
        String packageName = getPackageName();
        SharedPreferences preferences = getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        deleteDatabase("ReactSafeDb");
    }

    void clearServices(){
        try {
            stopService(new Intent(this, PoliceForegroundService.class));
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            stopService(new Intent(this, ParentForegroundService.class));
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            stopService(new Intent(this, HospitalForegroundService.class));
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            stopService(new Intent(this, AccidentDetectionService.class));
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            stopService(new Intent(this, PoliceForegroundService.class));
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            stopService(new Intent(this, AmbulanceForegroundService.class));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void cancelNotification() {
        try {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) getSystemService(ns);
            nMgr.cancelAll();
        }catch (Exception e){
            e.printStackTrace();

        }
    }
}