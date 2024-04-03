package jr.project.reactsafe.admin;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
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

                        binding.notPaired.setVisibility(View.GONE);
                        binding.linkedDeviceHolder.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

    }
}