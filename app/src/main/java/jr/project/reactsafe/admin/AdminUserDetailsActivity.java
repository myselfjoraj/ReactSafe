package jr.project.reactsafe.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Locale;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityAdminUserDetailsBinding;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;

public class AdminUserDetailsActivity extends AppCompatActivity {

    ActivityAdminUserDetailsBinding binding;
    UserModel model;
    boolean check = false;
    boolean isUser = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        if (model.getTitle().equals("user") || model.getTitle().equals("parent")){
            isUser = true;
        }

        if (isUser){
            setPairedDev();
        }else {
            binding.pairTitle.setVisibility(View.GONE);
            binding.notPaired.setVisibility(View.VISIBLE);
        }

        check = model.isBlocked();
        binding.switch1.setChecked(check);

        binding.switch1.setOnCheckedChangeListener((buttonView, isChecked) -> check = isChecked);

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
}