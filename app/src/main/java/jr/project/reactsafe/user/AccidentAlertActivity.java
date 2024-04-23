package jr.project.reactsafe.user;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import jr.project.reactsafe.ApplicationController;
import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityAccidentAlertBinding;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.NearestSafe;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.LocationModel;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.parent.ParentMainActivity;
import jr.project.reactsafe.parent.ParentPreferenceHelper;
import jr.project.reactsafe.parent.ParentSnoozeActivity;

public class AccidentAlertActivity extends AppCompatActivity {

    ActivityAccidentAlertBinding binding;
    int sec = 60;
    boolean isT = false;
    boolean isParent = false;
    boolean isToClose = false;
    Runnable timerRunnable;
    SharedPreference mPref;
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

        binding = ActivityAccidentAlertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mPref = new SharedPreference(AccidentAlertActivity.this);

        binding.progress.setMax(60);

        long defaultSec = Extras.getTimestamp() + 60000;
        sec = (int) ((mPref.getLong("startedAlertOn",defaultSec)/1000) - (Extras.getTimestamp()/1000));

//        Handler timerHandler = new Handler();
//        timerRunnable = new Runnable() {
//            @Override
//            public void run() {
//                runOnUiThread(() -> {
//                    binding.count.setText(sec + "");
//                    binding.progress.setProgress(sec);
//                });
//                sec --;
//                if (sec == 0){
//                    timerHandler.removeCallbacksAndMessages(timerRunnable);
//                    startActivity();
//                }
//                timerHandler.postDelayed(this, 1000);
//            }
//        };
//
//        timerRunnable.run();

        if (isToClose || (sec < 1)){
            Intent i = new Intent();
            if (isParent){
                i.setClass(AccidentAlertActivity.this, ParentMainActivity.class);
            }else {
                i.setClass(AccidentAlertActivity.this, UserMainActivity.class);
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finishAffinity();
        }

        CountDownTimer timer = new CountDownTimer(sec* 1000L, 1000){
            public void onTick(long millisUntilFinished){
                sec --;
                runOnUiThread(() -> {
                    binding.count.setText(sec + "");
                    binding.progress.setProgress(sec);
                });
            }
            public  void onFinish(){
                if (!isT) {
                    isToClose = true;
                    //forceInsertAlertInNodes();
                    InsertAlertInNodesModified();
                    ApplicationController.releaseMediaPlayer();
                    startActivity();
                }
            }
        };

        timer.start();

        String s = new SharedPreference(this).getUserTypeInPref();
        if (Objects.equals(s,"parent")){
            binding.swipeSnooze.setVisibility(View.VISIBLE);
            isParent = true;
        }else {
            binding.swipeSnooze.setVisibility(View.GONE);
        }

        binding.swipeBtn.setOnStateChangeListener(active -> {
            if (active){
                if (!isT) {
                    //removeAlerts(false);
                    mPref.putBoolean("isAlertDismissed",true);
                    isT = true;
                    ApplicationController.releaseMediaPlayer();
                    Intent i = new Intent();
                    if (isParent){
                        i.setClass(AccidentAlertActivity.this, ParentMainActivity.class);
                        new ParentPreferenceHelper(this).setIsOnAccident(null);
                    }else {
                        i.setClass(AccidentAlertActivity.this, UserMainActivity.class);
                    }
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("fromIntent","AccidentAlertActivity.class");
                    ApplicationController.releaseMediaPlayer();
                    startActivity(i);
                    finishAffinity();
                }
            }
        });

        binding.swipeSnooze.setOnStateChangeListener(active -> {
            if (active){
                if (!isT){
                   // removeAlerts();
                    isT = true;
                    ApplicationController.releaseMediaPlayer();
                    Intent i = new Intent();
                    i.setClass(AccidentAlertActivity.this, ParentSnoozeActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("fromIntent","AccidentAlertActivity.class");
                    ApplicationController.releaseMediaPlayer();
                    startActivity(i);
                    finishAffinity();
                }
            }
        });

        binding.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //forceInsertAlertInNodes();
                InsertAlertInNodesModified();

                ApplicationController.releaseMediaPlayer();
                Intent i = new Intent();
                if (isParent){
                    i.setClass(AccidentAlertActivity.this, ParentMainActivity.class);
                }else {
                    i.setClass(AccidentAlertActivity.this, UserMainActivity.class);
                }
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("fromIntent","AccidentAlertActivity.class");
                ApplicationController.releaseMediaPlayer();
                startActivity(i);
                finishAffinity();
            }
        });

    }

//    void removeAlerts(boolean i){
//        String uid;
//        if (isParent){
//            uid = new ParentPreferenceHelper(this).getPairedDeviceDetails().get(0).getUid();
//        }else {
//            uid = FirebaseAuth.getInstance().getUid();
//        }
//        long ts = mPref.getLong("startedAlertOn");
//        FirebaseHelper.RemoveAlert(uid);
//        if (!i){
//            if (isParent) {
//                String t = new SharedPreference(AccidentAlertActivity.this).getString("capturedTime",ts+"");
//                FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("alerts")
//                        .child("status").setValue("3");
//                new DatabaseHelper(this).updateAlert(t + "", "3");
//            }else {
//                FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("alerts")
//                        .child("status").setValue("2");
//                new DatabaseHelper(this).updateAlert(ts + "", "2");
//            }
//        }
//    }

    void startActivity(){
        if (!isT) {
            isT = true;
            Intent i = new Intent();
            if (isParent){
                i.setClass(AccidentAlertActivity.this, ParentMainActivity.class);
            }else {
                i.setClass(AccidentAlertActivity.this, UserMainActivity.class);
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finishAffinity();
        }
    }

    void forceInsertAlertInNodes(){
        String uid;
        if (isParent){
            uid = new ParentPreferenceHelper(this).getPairedDeviceDetails().get(0).getUid();
        }else {
            uid = FirebaseAuth.getInstance().getUid();
        }
        FirebaseDatabase.getInstance().getReference().child("alert")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()){
                            return;
                        }
                        long p = new SharedPreference(AccidentAlertActivity.this)
                                .getLong("startedAlertOn", Extras.getTimestamp());
                        String ts = snapshot.child("timestamp").getValue(String.class);
                        if (ts == null){
                            ts = p+"";
                        }
                        LocationModel l = new LocationModel(loc().get(0)+"",loc().get(1)+"",
                                FirebaseAuth.getInstance().getUid(),ts);


                        String police    = snapshot.child("police").getValue(String.class);
                        String ambulance = snapshot.child("ambulance").getValue(String.class);
                        String hospital  = snapshot.child("hospital").getValue(String.class);
                        if (police!=null) {
                            FirebaseHelper.InsertAlertOnPoliceId(police, l);
                        }
                        if (ambulance!=null) {
                            FirebaseHelper.InsertAlertOnAmbulanceId(ambulance, l);
                        }
                        if (hospital!=null) {
                            FirebaseHelper.InsertAlertOnHospitalId(hospital, l);
                        }
                        if (isParent){
                            //removeAlerts(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public ArrayList<Double> loc(){
        ArrayList<Double> loc = new ArrayList<>();
        try {

            double lati = Double.parseDouble(new SharedPreference(AccidentAlertActivity.this)
                    .getString("lastLatitude"));
            double longi = Double.parseDouble(new SharedPreference(AccidentAlertActivity.this)
                    .getString("lastLongitude"));

            loc.add(lati);
            loc.add(longi);
        }catch (Exception e){
            e.printStackTrace();
        }

        return loc;
    }




    String policeId = null;
    String ambulanceId = null;
    String hospitalId = null;

    void InsertAlertInNodesModified(){
        mPref.putBoolean("isAlertDismissed",true);
        policeId = mPref.getString("nearPolice");
        ambulanceId = mPref.getString("nearAmbulance");
        hospitalId = mPref.getString("nearHospital");
        String parentId = new UserPreferenceHelper(this).getPairedDeviceDetails().get(0).getUid();

        String ts = mPref.getLong("startedAlertOn", (Extras.getTimestamp())) + "";
        LocationModel locationModel = new LocationModel(loc().get(0)+"",loc().get(1)+"",
                FirebaseAuth.getInstance().getUid(),ts);
        if (policeId!=null){
            FirebaseHelper.InsertAlertOnPoliceId(policeId,locationModel);
        }

        if (ambulanceId!=null){
            FirebaseHelper.InsertAlertOnAmbulanceId(ambulanceId,locationModel);
        }

        if (hospitalId!=null){
            FirebaseHelper.InsertAlertOnHospitalId(hospitalId,locationModel);
        }

        if (parentId!=null){
            FirebaseHelper.InsertAlertOnParentId(parentId,locationModel);
        }
    }

}