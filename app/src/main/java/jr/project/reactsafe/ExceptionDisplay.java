package jr.project.reactsafe;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import jr.project.reactsafe.ambulance.AmbulanceMainActivity;
import jr.project.reactsafe.extras.auth.LoginActivity;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.hospital.HospitalMainActivity;
import jr.project.reactsafe.parent.PairUserDeviceActivity;
import jr.project.reactsafe.parent.ParentMainActivity;
import jr.project.reactsafe.parent.ParentPreferenceHelper;
import jr.project.reactsafe.police.PoliceMainActivity;
import jr.project.reactsafe.user.UserMainActivity;

public class ExceptionDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_display);

        try {
            String er = getIntent().getExtras().getString("error");
            FirebaseDatabase.getInstance().getReference().child("errors")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child(String.valueOf(Extras.getTimestamp())).setValue(er);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            getActivity();


            TextView exception_text = (TextView) findViewById(R.id.textView43);
            ImageView btnBack = findViewById(R.id.backBtn);
            Button send = findViewById(R.id.button2);
            exception_text.setText(getIntent().getExtras().getString("error"));

            btnBack.setOnClickListener(view -> intentData());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        intentData();
    }

    public void intentData() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(ExceptionDisplay.this, SplashScreenActivity.class);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    void getActivity(){
        String s = new SharedPreference(this).getUserTypeInPref();
        switch (s){
            case "user":
                startActivity(new Intent(this, UserMainActivity.class));
                finishAffinity();
                Log.e("ReactSafeLoginSystem"," user called");
                break;
            case "parent":
                if (new ParentPreferenceHelper(this).getPairedDeviceDetails() == null){
                    startActivity(new Intent(this, PairUserDeviceActivity.class));
                    finishAffinity();
                    Log.e("ReactSafeLoginSystem","pair user called");
                }else {
                    startActivity(new Intent(this, ParentMainActivity.class));
                    finishAffinity();
                    Log.e("ReactSafeLoginSystem","parent called");
                }
                break;
            case "ambulance":
                startActivity(new Intent(this, AmbulanceMainActivity.class));
                finishAffinity();
                Log.e("ReactSafeLoginSystem","ambulance called");
                break;
            case "hospital":
                startActivity(new Intent(this, HospitalMainActivity.class));
                finishAffinity();
                Log.e("ReactSafeLoginSystem","hospital called");
                break;
            case "police":
                startActivity(new Intent(this, PoliceMainActivity.class));
                finishAffinity();
                Log.e("ReactSafeLoginSystem","police called");
                break;
            default:
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
                Log.e("ReactSafeLoginSystem","login called");
                break;

        }
    }
}