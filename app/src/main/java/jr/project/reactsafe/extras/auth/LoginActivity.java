package jr.project.reactsafe.extras.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.SplashScreenActivity;
import jr.project.reactsafe.databinding.ActivityLoginBinding;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.user.UserMainActivity;
import jr.project.reactsafe.user.UserPreferenceHelper;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        binding.loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateLogin();
            }
        });


    }

    private void validateLogin() {
        String email = binding.emailTypeField.getText().toString();
        String pass  = binding.passwordTypeField.getText().toString();

        if (email.isEmpty()){
            Toast.makeText(this, "Enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.isEmpty()){
            Toast.makeText(this, "Enter a valid password", Toast.LENGTH_SHORT).show();
            return;
        }

        initiateLogin(email,pass);

    }


    void initiateLogin(String email, String password){

        if (email.isEmpty()){
            Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()){
            Toast.makeText(this, "Please enter a valid password!", Toast.LENGTH_SHORT).show();
            return;
        }

        // show progress
        showPleaseWaitDialog("Logging in ...");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {
                    // end progress
                    dismissPleaseWaitDialog();
                    if (task.isSuccessful()){
                        FirebaseHelper.getUser(FirebaseAuth.getInstance().getUid(), new FirebaseHelper.OnReceivedUser() {
                            @Override
                            public void getReceiver(UserModel model) {
                                new UserPreferenceHelper(LoginActivity.this).setProfileName(model.getName());
                                new UserPreferenceHelper(LoginActivity.this).setProfileImage(model.getProfileImage());
                                new UserPreferenceHelper(LoginActivity.this).setProfileNumber(model.getPhone());

                                new SharedPreference(LoginActivity.this).setUserTypeInPref(model.getTitle());
                                startActivity(new Intent(LoginActivity.this, SplashScreenActivity.class));
                            }
                        });
                        //startActivity(new Intent(LoginActivity.this, UserMainActivity.class));
                    }else {
                        Toast.makeText(LoginActivity.this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showPleaseWaitDialog(String msg) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissPleaseWaitDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}