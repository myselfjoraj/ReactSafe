package jr.project.reactsafe;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import jr.project.reactsafe.ambulance.AmbulanceMainActivity;
import jr.project.reactsafe.extras.auth.LoginActivity;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.hospital.HospitalMainActivity;
import jr.project.reactsafe.parent.PairUserDeviceActivity;
import jr.project.reactsafe.parent.ParentMainActivity;
import jr.project.reactsafe.parent.ParentPreferenceHelper;
import jr.project.reactsafe.police.PoliceMainActivity;
import jr.project.reactsafe.user.UserMainActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

//        FirebaseDatabase.getInstance().getReference().child("alert").removeValue();
//        FirebaseDatabase.getInstance().getReference().child("ambulance").removeValue();
//        FirebaseDatabase.getInstance().getReference().child("hospital").removeValue();
//        FirebaseDatabase.getInstance().getReference().child("police").removeValue();
//        FirebaseDatabase.getInstance().getReference().child("users").removeValue();

//        try {
//            mAuth.signOut();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        Thread thread = new Thread(() -> {
            try {
                sleep(3000);
                if (mAuth.getUid() != null){
                    getActivity();
                }else {
                    startActivity(new Intent(this,LoginActivity.class));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }



    void getActivity(){
        String s = new SharedPreference(this).getUserTypeInPref();
        switch (s){
            case "user":
                startActivity(new Intent(this, UserMainActivity.class));
                Log.e("ReactSafeLoginSystem"," user called");
                break;
            case "parent":
                if (new ParentPreferenceHelper(this).getPairedDeviceDetails() == null){
                    startActivity(new Intent(this, PairUserDeviceActivity.class));
                    Log.e("ReactSafeLoginSystem","pair user called");
                }else {
                    startActivity(new Intent(this, ParentMainActivity.class));
                    Log.e("ReactSafeLoginSystem","parent called");
                }
                break;
            case "ambulance":
                startActivity(new Intent(this, AmbulanceMainActivity.class));
                Log.e("ReactSafeLoginSystem","ambulance called");
                break;
            case "hospital":
                startActivity(new Intent(this, HospitalMainActivity.class));
                Log.e("ReactSafeLoginSystem","hospital called");
                break;
            case "police":
                startActivity(new Intent(this, PoliceMainActivity.class));
                Log.e("ReactSafeLoginSystem","police called");
                break;
            default:
                startActivity(new Intent(this,LoginActivity.class));
                Log.e("ReactSafeLoginSystem","login called");
                break;

        }
    }

}