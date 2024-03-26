package jr.project.reactsafe.parent;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityParentSnoozeBinding;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.LocationModel;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.user.AccidentAlertActivity;

public class ParentSnoozeActivity extends AppCompatActivity {

    ActivityParentSnoozeBinding binding;
    Runnable timerRunnable;
    int sec = 60;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        binding = ActivityParentSnoozeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Handler timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    binding.count.setText(sec + "");
                    binding.progress.setProgress(sec);
                });
                sec --;
                if (sec == 0){
                    timerHandler.removeCallbacksAndMessages(timerRunnable);
                    forceInsertAlertInNodes();
                }
                timerHandler.postDelayed(this, 1000);
            }
        };

        timerRunnable.run();


        binding.swipeBtn.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                if (active){
                    removeAll(false);
                    new ParentPreferenceHelper(ParentSnoozeActivity.this).setIsOnAccident(null);
                }
            }
        });

        binding.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forceInsertAlertInNodes();
                startActivity(new Intent(ParentSnoozeActivity.this,ParentAccidentProceedings.class));
                finish();
            }
        });

    }

    void removeAll(boolean i){
        FirebaseHelper.RemoveAlert(new ParentPreferenceHelper(this).getPairedDeviceDetails().get(0).getUid());
        startActivity(new Intent(ParentSnoozeActivity.this,ParentMainActivity.class));
        if (!i){
            String ts = new SharedPreference(this).getString("capturedTime","");
            FirebaseDatabase.getInstance().getReference().child("users").child(new ParentPreferenceHelper(this)
                            .getPairedDeviceDetails().get(0).getUid()).child("alerts")
                    .child("status").setValue("3");
            new DatabaseHelper(this).updateAlert(ts + "", "3");
        }
        finishAffinity();
    }

    void forceInsertAlertInNodes(){
        FirebaseDatabase.getInstance().getReference().child("alert")
                .child(new ParentPreferenceHelper(this).getPairedDeviceDetails().get(0).getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        if (!snapshot1.exists()){
                            return;
                        }
                        long p = new SharedPreference(ParentSnoozeActivity.this)
                                .getLong("startedAlertOn", Extras.getTimestamp());
                        LocationModel l = new LocationModel(loc().get(0)+"",loc().get(1)+"",
                                FirebaseAuth.getInstance().getUid(),p+"");

                        String police    = snapshot1.child("police").getValue(String.class);
                        String ambulance = snapshot1.child("ambulance").getValue(String.class);
                        String hospital  = snapshot1.child("hospital").getValue(String.class);
                        if (police!=null) {
                            FirebaseHelper.InsertAlertOnPoliceId(police, l);
                        }
                        if (ambulance!=null) {
                            FirebaseHelper.InsertAlertOnAmbulanceId(ambulance, l);
                        }
                        if (hospital!=null) {
                            FirebaseHelper.InsertAlertOnHospitalId(hospital, l);
                        }
                        removeAll(true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public ArrayList<Double> loc(){
        ArrayList<Double> loc = new ArrayList<>();
        try {

            double lati = Double.parseDouble(new SharedPreference(ParentSnoozeActivity.this)
                    .getString("lastLatitude"));
            double longi = Double.parseDouble(new SharedPreference(ParentSnoozeActivity.this)
                    .getString("lastLongitude"));

            loc.add(lati);
            loc.add(longi);
        }catch (Exception e){
            e.printStackTrace();
        }

        return loc;
    }
}