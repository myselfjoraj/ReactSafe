package jr.project.reactsafe.user;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.SplashScreenActivity;
import jr.project.reactsafe.ambulance.AmbulanceMainActivity;
import jr.project.reactsafe.databinding.ActivityUserSettingsBinding;
import jr.project.reactsafe.extras.auth.LoginActivity;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.RecentModel;
import jr.project.reactsafe.extras.util.CircleImageView;
import jr.project.reactsafe.hospital.HospitalMainActivity;
import jr.project.reactsafe.parent.PairUserDeviceActivity;
import jr.project.reactsafe.parent.ParentMainActivity;
import jr.project.reactsafe.parent.ParentPreferenceHelper;
import jr.project.reactsafe.police.PoliceMainActivity;

public class UserSettingsActivity extends AppCompatActivity {

    ActivityUserSettingsBinding binding;
    UserPreferenceHelper mPref;
    ActivityResultLauncher<Intent> resultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPref = new UserPreferenceHelper(this);

        Glide.with(UserSettingsActivity.this)
                .load(mPref.getProfileImage())
                .placeholder(R.drawable.avatar)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.circleImageView);

        binding.name.setText(mPref.getProfileName());
        binding.email.setText(mPref.getProfileEmail());

        setResultLauncher();
        setRatio();

        binding.accountSettings.setOnClickListener(v -> showAccountSettingsBottomSheet());

        binding.pairDevice.setOnClickListener(v -> startActivity(new Intent(UserSettingsActivity.this,PairedDevicesActivity.class)));

        binding.signOut.setOnClickListener(v -> showSignOutDialog());

        String s = new SharedPreference(this).getUserTypeInPref();
        //Toast.makeText(this, "---"+s, Toast.LENGTH_SHORT).show();
        boolean isDisp = Objects.equals("user",s) || Objects.equals("parent",s);
        if (s!=null){
            if (!isDisp){
                binding.pairDevice.setVisibility(View.GONE);
                try (DatabaseHelper helper = new DatabaseHelper(UserSettingsActivity.this)){
                    String t = " ";
                    if (s.equals("ambulance")) {
                        t = helper.readAmbulanceAccepts().size() + " Accidents Received";
                    }else if (s.equals("hospital")) {
                        t = helper.readHospitalAccepts().size() + " Accidents Received";
                    }else if (s.equals("police")){
                        t = helper.readPoliceAccepts().size() + " Accidents Received";
                    }else {
                        t = helper.readRecentFalls().size() + " Accidents Detected";
                    }
                    binding.happenedAccidents.setText(t);
                }
            }else {
                binding.pairDevice.setVisibility(View.VISIBLE);
            }
        }

        if (new UserPreferenceHelper(this).getIAmAdmin()){
            binding.accCard.setVisibility(View.GONE);
            binding.accountSettings.setVisibility(View.GONE);
        }

    }

    int occurredAccidents = 0;
    int totalDetections = 0;
    void setRatio(){
        try (DatabaseHelper db = new DatabaseHelper(this)){
            ArrayList<RecentModel> models = db.readRecentFalls();
            if (models!=null && !models.isEmpty()){
                for (RecentModel rm : models){
                    if (rm.getStatus().equals("1")){
                        occurredAccidents++;
                    }
                    totalDetections ++;
                }
            }
        }

        binding.happenedAccidents.setText(occurredAccidents+" Accidents Happened");
        int ratio = 5;
        if (occurredAccidents>0 && totalDetections>0)
            ratio = (occurredAccidents / totalDetections) * 100;

        binding.progressBar.setProgress(ratio);
    }

    Uri profileUri = null;
    String uid  = FirebaseAuth.getInstance().getUid();
    public void showAccountSettingsBottomSheet(){
        BottomSheetDialog bottomSheetDialog;
        bottomSheetDialog = new BottomSheetDialog(UserSettingsActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_account_edit);

        CircleImageView pImage  = bottomSheetDialog.findViewById(R.id.profileImage);
        EditText  nameBox = bottomSheetDialog.findViewById(R.id.nameField);
        EditText  phoneBox = bottomSheetDialog.findViewById(R.id.phoneField);
        Button    save    = bottomSheetDialog.findViewById(R.id.save);

        String name = mPref.getProfileName();
        String email = mPref.getProfileEmail();
        String link = mPref.getProfileImage();
        String phone = mPref.getProfileNumber();

        if (!Objects.equals(name,email)){
            assert nameBox != null;
            nameBox.setText(mPref.getProfileName());
        }

        if (phone!=null)
            phoneBox.setText(phone);

        if (link!=null) {
            assert pImage != null;
            Glide.with(UserSettingsActivity.this)
                    .load(link)
                    .placeholder(R.drawable.avatar)
                    .into(pImage);
        }

        if (profileUri!=null){
            assert pImage != null;
            Glide.with(UserSettingsActivity.this)
                    .load(profileUri)
                    .into(pImage);
        }

        Objects.requireNonNull(pImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                // Initialize intent
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // set type
                intent.setType("image/*");
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // Launch intent
                resultLauncher.launch(intent);
            }
        });

        assert save != null;
        save.setOnClickListener(v -> {

            String new_name = nameBox.getText().toString();
            String new_phone = phoneBox.getText().toString();
            if (new_name.isEmpty()){
                Toast.makeText(UserSettingsActivity.this,
                        "Please enter your name!", Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (new_phone.isEmpty()){
                Toast.makeText(UserSettingsActivity.this,
                        "Please enter your phone number!", Toast.LENGTH_SHORT
                ).show();
                return;
            }else if (new_phone.length() != 10 ){
                Toast.makeText(UserSettingsActivity.this,
                        "Invalid phone number!", Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (profileUri!=null) {
                FirebaseStorage.getInstance().getReference().child("profileImages")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).putFile(profileUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            Objects.requireNonNull(taskSnapshot.getMetadata().getReference()).getDownloadUrl()
                                    .addOnCompleteListener(task -> {
                                        String profileImageLink = String.valueOf(task.getResult());
                                        throwToDb(profileImageLink);
                                        mPref.setProfileImage(profileImageLink);
                                    });
                        });
            }

            throwToDb(new_name,new_phone);
            mPref.setProfileName(new_name);
            mPref.setProfileNumber(new_phone);

            //re setting views
            binding.name.setText(mPref.getProfileName());
            if (profileUri!=null)
                Glide.with(UserSettingsActivity.this)
                    .load(profileUri)
                    .into(binding.circleImageView);

            bottomSheetDialog.dismiss();
        });


        bottomSheetDialog.show();
    }
    void throwToDb(String name,String phone){
        String uid = "admin";
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            uid = FirebaseAuth.getInstance().getUid();
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid);
        ref.child("phone").setValue(phone);
        ref.child("name").setValue(name);
        String s = new SharedPreference(this).getUserTypeInPref();
        if (s!=null && (!s.equals("user") || !s.equals("parent"))){
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child(s)
                    .child(FirebaseAuth.getInstance().getUid());
            ref2.child("phone").setValue(phone);
            ref2.child("name").setValue(name);
        }
    }

    void throwToDb(String profileImage){
        String uid = "admin";
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            uid = FirebaseAuth.getInstance().getUid();
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid);
        ref.child("profileImage").setValue(profileImage);
        String s = new SharedPreference(this).getUserTypeInPref();
        if (s!=null && (!s.equals("user") || !s.equals("parent"))){
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child(s)
                    .child(FirebaseAuth.getInstance().getUid());
            ref2.child("profileImage").setValue(profileImage);
        }
    }

    // Initialize result launcher
    public void setResultLauncher() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    // Initialize result data
                    Intent data = result.getData();
                    // check condition
                    if (data != null) {
                        // Get uri
                        profileUri = data.getData();

                        showAccountSettingsBottomSheet();
                    }
                });
    }

    public void showSignOutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingsActivity.this);
        builder.setTitle("Sign Out?")
                .setMessage("Are you sure you want to sign out of Cloud Box?")
                .setPositiveButton("NO", (dialog, which) -> { })
                .setNegativeButton("YES", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    mPref.setIAmAdmin(false);
                    cancelNotification();
                    clearData();
                    startActivity(new Intent(UserSettingsActivity.this, SplashScreenActivity.class));
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