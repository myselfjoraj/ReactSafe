package jr.project.reactsafe.user;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.SplashScreenActivity;
import jr.project.reactsafe.databinding.ActivityUserSettingsBinding;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.model.RecentModel;
import jr.project.reactsafe.extras.util.CircleImageView;

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
        Button    save    = bottomSheetDialog.findViewById(R.id.save);

        String name = mPref.getProfileName();
        String email = mPref.getProfileEmail();
        String link = mPref.getProfileImage();

        if (!Objects.equals(name,email)){
            assert nameBox != null;
            nameBox.setText(mPref.getProfileName());
        }

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
            if (new_name.isEmpty()){
                Toast.makeText(UserSettingsActivity.this,
                        "Please enter your name!", Toast.LENGTH_SHORT
                ).show();
                return;
            }else if (new_name.length() > 22 ){
                Toast.makeText(UserSettingsActivity.this,
                        "Your name exceeds the character limit of 22!", Toast.LENGTH_SHORT
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
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                                                .child("profileImage").setValue(profileImageLink);
                                        mPref.setProfileImage(profileImageLink);
                                    });
                        });
            }

            FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                    .child("name").setValue(new_name);
            mPref.setProfileName(new_name);

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
                    ((ActivityManager) UserSettingsActivity.this
                            .getSystemService(Context.ACTIVITY_SERVICE))
                            .clearApplicationUserData();
                    startActivity(new Intent(UserSettingsActivity.this, SplashScreenActivity.class));
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}