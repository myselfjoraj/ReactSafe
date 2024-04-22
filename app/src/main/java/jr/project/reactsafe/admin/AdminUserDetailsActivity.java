package jr.project.reactsafe.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;

import java.util.Locale;
import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityAdminUserDetailsBinding;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.user.UserPreferenceHelper;
import jr.project.reactsafe.user.UserSettingsActivity;

public class AdminUserDetailsActivity extends AppCompatActivity {

    ActivityAdminUserDetailsBinding binding;
    UserModel model;
    boolean check = false;
    boolean isUser = false;
    Uri profileUri;
    ActivityResultLauncher<Intent> resultLauncher;
    ProgressDialog progressDialog;
    UserPreferenceHelper mPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setResultLauncher();

        mPref = new UserPreferenceHelper(AdminUserDetailsActivity.this);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String m = getIntent().getStringExtra("model");
        model = new Gson().fromJson(m, UserModel.class);

        if (model.getProfileImage()!=null){
            Glide.with(AdminUserDetailsActivity.this)
                    .load(model.getProfileImage())
                    .placeholder(R.drawable.avatar)
                    .into(binding.profileImage);
        }

        binding.title.setText(model.getTitle().toUpperCase(Locale.ROOT)+" DEVICE");
        binding.uid.setText(model.getUid());

        binding.nameField.setText(model.getName());
        binding.emailField.setText(model.getEmail());
        binding.numberField.setText(model.getPhone());

        binding.emailField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminUserDetailsActivity.this, "Email id cannot be edited!", Toast.LENGTH_SHORT).show();
            }
        });

        if (model.getTitle().equals("user") || model.getTitle().equals("parent")){
            isUser = true;
        }

        if (isUser){
            setPairedDev();
        }else {
            binding.pairTitle.setVisibility(View.GONE);
            binding.notPaired.setVisibility(View.GONE);
        }

        check = model.isBlocked();
        binding.switch1.setChecked(check);

        binding.switch1.setOnCheckedChangeListener((buttonView, isChecked) -> check = isChecked);

        // select profileImage
        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            resultLauncher.launch(intent);
        });

        binding.save.setOnClickListener(v -> {
            showPleaseWaitDialog("Updating ...");
            String new_name = binding.nameField.getText().toString();
            String new_phone = binding.numberField.getText().toString();
            if (new_name.isEmpty()){
                Toast.makeText(AdminUserDetailsActivity.this,
                        "Please enter your name!", Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (new_phone.isEmpty()){
                Toast.makeText(AdminUserDetailsActivity.this,
                        "Please enter your phone number!", Toast.LENGTH_SHORT
                ).show();
                return;
            }else if (new_phone.length() != 10 ){
                Toast.makeText(AdminUserDetailsActivity.this,
                        "Invalid phone number!", Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (profileUri!=null) {
                dismissPleaseWaitDialog();
                showPleaseWaitDialog("Uploading Image ...");
                FirebaseStorage.getInstance().getReference().child("profileImages")
                        .child(model.getUid()).putFile(profileUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            Objects.requireNonNull(taskSnapshot.getMetadata().getReference()).getDownloadUrl()
                                    .addOnCompleteListener(task -> {
                                        String profileImageLink = String.valueOf(task.getResult());
                                        throwToDb(profileImageLink);
                                        throwToDb(new_name, new_phone);
                                        dismissPleaseWaitDialog();
                                        Toast.makeText(this, "Profile updated successfully ...", Toast.LENGTH_SHORT).show();
                                    });
                        });
            }else {
                throwToDb(new_name, new_phone);
                dismissPleaseWaitDialog();
                Toast.makeText(this, "Profile updated successfully ...", Toast.LENGTH_SHORT).show();
            }

            blockUnblock(check);


        });

    }
    public void setResultLauncher() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    // Initialize result data
                    Intent data = result.getData();
                    // check condition
                    if (data != null) {
                        // Get uri
                        profileUri = data.getData();
                        Glide.with(this)
                                .load(profileUri)
                                .into(binding.profileImage);
                    }
                });
    }

    void throwToDb(String name,String phone){
        String uid = model.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid);
        ref.child("phone").setValue(phone);
        ref.child("name").setValue(name);
        String s = model.getTitle();
        if (s!=null && (!s.equals("user") || !s.equals("parent"))){
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child(s)
                    .child(uid);
            ref2.child("phone").setValue(phone);
            ref2.child("name").setValue(name);
        }
    }

    void throwToDb(String profileImage){
        String uid = model.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid);
        ref.child("profileImage").setValue(profileImage);
        String s = model.getTitle();
        if (s!=null && (!s.equals("user") || !s.equals("parent"))){
            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child(s)
                    .child(uid);
            ref2.child("profileImage").setValue(profileImage);
        }
    }

    void setPairedDev(){
        if (model.getPairedBy() == null){
            binding.notPaired.setVisibility(View.VISIBLE);
            binding.linkedDeviceHolder.setVisibility(View.GONE);
        }else {
            FirebaseHelper.getUser(model.getPairedBy(), new FirebaseHelper.OnReceivedUser() {
                @Override
                public void getReceiver(UserModel model) {
                    if (model!=null) {

                        if (model.getProfileImage()!=null){
                            Glide.with(AdminUserDetailsActivity.this)
                                    .load(model.getProfileImage())
                                    .placeholder(R.drawable.avatar)
                                    .into(binding.conIv);
                        }
                        binding.conName.setText(model.getName());
                        binding.conDate.setText("connected on "+ Extras.getStandardFormDateFromTimeStamp(model.getPairedOn()));

                        binding.linkedDeviceHolder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(AdminUserDetailsActivity.this,AdminUserDetailsActivity.class);
                                i.putExtra("model",new Gson().toJson(model));
                                startActivity(i);
                            }
                        });

                        binding.notPaired.setVisibility(View.GONE);
                        binding.linkedDeviceHolder.setVisibility(View.VISIBLE);
                    }
                }
            });

        }
    }

    void blockUnblock(boolean a){
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(model.getUid()).child("blocked").setValue(a);
    }

    public void showPleaseWaitDialog(String msg) {
        progressDialog = new ProgressDialog(AdminUserDetailsActivity.this);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dismissPleaseWaitDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}