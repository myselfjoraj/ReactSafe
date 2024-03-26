package jr.project.reactsafe.extras.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.SplashScreenActivity;
import jr.project.reactsafe.databinding.ActivityRegisterBinding;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.UserModel;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    ProgressDialog progressDialog;
    UserModel model;
    int title = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.regEntity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, RegisterEntityActivity.class));
            }
        });

        binding.signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        binding.radioButtonUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                title = 1;
                binding.radioButtonParent.setChecked(false);
            }
        });

        binding.radioButtonParent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                title = 2;
                binding.radioButtonUser.setChecked(false);
            }
        });

        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateMyForm();
            }
        });

    }


    void ValidateMyForm(){

        String name = binding.etName.getText().toString();
        String email = binding.etEmail.getText().toString();
        String pass = binding.etPassword.getText().toString();

        if (name.isEmpty()){
            Toast.makeText(this, "Please enter a valid name.", Toast.LENGTH_SHORT).show();
            return;
        }else if (email.isEmpty()){
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
            }
            return;
        }else if (pass.isEmpty() || pass.length() < 6){
            Toast.makeText(this, "Password should be greater than six characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        showPleaseWaitDialog("Please wait...");
        model = new UserModel();
        model.setName(name);
        model.setEmail(email);

        if (title == 1){
            model.setTitle("user");
        }else if (title == 2){
            model.setTitle("parent");
        }

        signInWIthFirebase(email,pass);

    }

    void signInWIthFirebase(String email, String password){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    dismissPleaseWaitDialog();
                    if (task.isSuccessful()) {
                        Log.d("ReactSafeFirebaseAuth", "createUserWithEmail:success");
                        model.setUid(FirebaseAuth.getInstance().getUid());
                        FirebaseHelper.InsertUser(model);
                        if (title == 1)
                            new SharedPreference(RegisterActivity.this).setUserTypeInPref("user");
                        else
                            new SharedPreference(RegisterActivity.this).setUserTypeInPref("parent");
                        startActivity(new Intent(RegisterActivity.this, SplashScreenActivity.class));
                    } else {
                        Log.w("ReactSafeFirebaseAuth","createUserWithEmail:failure  "+ task.getException().getMessage());
                        if (Objects.requireNonNull(task.getException().getMessage()).startsWith("The email address is already in use by another account.")){
                            Toast.makeText(this, "User Registered! Please Log In.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                        }
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