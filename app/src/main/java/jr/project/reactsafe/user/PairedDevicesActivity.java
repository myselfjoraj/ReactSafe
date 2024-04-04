package jr.project.reactsafe.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.SplashScreenActivity;
import jr.project.reactsafe.databinding.ActivityPairedDevicesBinding;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;

public class PairedDevicesActivity extends AppCompatActivity {

    ActivityPairedDevicesBinding binding;
    boolean isAvailable;
    ArrayList<UserModel> models;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPairedDevicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(v -> finish());

        models = new UserPreferenceHelper(PairedDevicesActivity.this)
                .getPairedDeviceDetails();

        isAvailable = models!=null && !models.isEmpty();

        if (models!=null && !models.isEmpty()){
            UserModel model = models.get(0);

            binding.linkedDeviceHolder.setVisibility(View.VISIBLE);
            binding.pairBtn.setText("Remove and Pair Another Device");

            Glide.with(PairedDevicesActivity.this)
                    .load(model.getProfileImage())
                    .placeholder(R.drawable.avatar)
                    .into(binding.conIv);

            binding.conName.setText(model.getName());
            binding.conDate.setText("connected on "+ Extras.getStandardFormDateFromTimeStamp(model.getPairedOn()));
        }

        binding.pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(new SharedPreference(PairedDevicesActivity.this).getUserTypeInPref(),"parent")){
                    deletePair();
                    startActivity(new Intent(PairedDevicesActivity.this, SplashScreenActivity.class));
                    finishAffinity();
                }else {
                    startActivity(new Intent(PairedDevicesActivity.this, PairParentActivity.class));
                    finish();
                }
            }
        });


        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePair();
            }
        });



    }

    void deletePair(){
        //
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getUid());
        dbRef.child("pairedBy").removeValue();
        dbRef.child("pairedOn").removeValue();
        if (models!=null && !models.isEmpty()){
            DatabaseReference dbRef2 = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(models.get(0).getUid());
            dbRef2.child("pairedBy").removeValue();
            dbRef2.child("pairedOn").removeValue();
        }
        //
        new UserPreferenceHelper(PairedDevicesActivity.this)
                .setPairedDeviceDetails(null);
    }
}