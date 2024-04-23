package jr.project.reactsafe.extras.auth;


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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    ActivityForgotPasswordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetLink();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    void sendResetLink(){
        String et = binding.editTextText.getText().toString();
        if (!et.isEmpty()){
            Toast.makeText(this, "Sending your reset link!", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().sendPasswordResetEmail(""+et)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Forget Password", "Email sent.");
                                Toast.makeText(ForgotPasswordActivity.this, "Reset Link Send Successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            }else {
                                Log.d("Forget Password", "exception -> "+task.getException());
                                if (task.getException().toString().startsWith("com.google.firebase.auth." +
                                        "FirebaseAuthInvalidCredentialsException: The email address is badly formatted")){
                                    Toast.makeText(ForgotPasswordActivity.this, "Please enter email in correct format!", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(ForgotPasswordActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }else {
            Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
        }
    }
}