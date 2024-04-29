package jr.project.reactsafe.extras.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import jr.project.reactsafe.admin.AdminMainActivity;
import jr.project.reactsafe.ambulance.AmbulanceMainActivity;
import jr.project.reactsafe.databinding.ActivityPasscodeBinding;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.hospital.HospitalMainActivity;
import jr.project.reactsafe.parent.PairUserDeviceActivity;
import jr.project.reactsafe.parent.ParentMainActivity;
import jr.project.reactsafe.parent.ParentPreferenceHelper;
import jr.project.reactsafe.police.PoliceMainActivity;
import jr.project.reactsafe.user.UserMainActivity;
import jr.project.reactsafe.user.UserPreferenceHelper;
import jr.project.reactsafe.user.UserSettingsActivity;

public class PasscodeActivity extends AppCompatActivity {

    ActivityPasscodeBinding binding;
    SharedPreference mPref;
    int TYPE_PASSCODE;
    String prevPass;
    public int PASSCODE_CHANGE = 0;
    public int PASSCODE_RETYPE = 1;
    public int PASSCODE_UNLOCK = 2;
    public int PASSCODE_SETUP  = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasscodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.et1.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.et1, InputMethodManager.SHOW_IMPLICIT);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

        mPref = new SharedPreference(PasscodeActivity.this);

        String var = getIntent().getStringExtra("value");

        switch (var) {
            case "null":
                binding.heading.setText("Setup Passcode");
                binding.passcodeDialog.setText("Please Enter A Passcode");
                TYPE_PASSCODE = PASSCODE_SETUP;
                break;
            case "exist":
                binding.heading.setText("Change Passcode");
                binding.passcodeDialog.setText("Please Enter Your Passcode");
                binding.remove.setVisibility(View.VISIBLE);
                TYPE_PASSCODE = PASSCODE_CHANGE;
                break;
            case "unlock":
                binding.passcodeDialog.setText("Please Enter Your Passcode");
                TYPE_PASSCODE = PASSCODE_UNLOCK;
                break;
            case "retype":
                binding.heading.setText("Retype Passcode");
                binding.passcodeDialog.setText("Please Confirm Your Passcode");
                prevPass = getIntent().getStringExtra("typedPasscode");
                TYPE_PASSCODE = PASSCODE_RETYPE;
                break;
        }

        binding.remove.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(PasscodeActivity.this);
            builder.setTitle("Remove Passcode?")
                    .setMessage("Are you sure you want to remove passcode?")
                    .setPositiveButton("NO", (dialog, which) -> { })
                    .setNegativeButton("YES", (dialog, which) -> {
                        mPref.removePasscodeForCB();
                        startActivity(
                                new Intent(PasscodeActivity.this, SplashScreenActivity.class)
                        );
                        finishAffinity();
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });


        binding.et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.et1.getText().length()>0){
                    binding.et2.requestFocus();
                }
            }
        });

        binding.et2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.et2.getText().length()>0){
                    binding.et3.requestFocus();
                }else {
                    binding.et1.requestFocus();
                }
            }
        });

        binding.et3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.et3.getText().length()>0){
                    binding.et4.requestFocus();
                }else {
                    binding.et2.requestFocus();
                }
            }
        });

        binding.et4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.et4.getText().length()>0){
                    checkAndStructurePasscode();
                }else {
                    binding.et3.requestFocus();
                }
            }
        });

        //////////////////////
        binding.et1.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == 0 || actionId== EditorInfo.IME_ACTION_DONE) {
                checkAndStructurePasscode();
            }
            return false;
        });
        binding.et2.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == 0 || actionId== EditorInfo.IME_ACTION_DONE) {
                checkAndStructurePasscode();
            }
            return false;
        });
        binding.et3.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == 0 || actionId== EditorInfo.IME_ACTION_DONE) {
                checkAndStructurePasscode();
            }
            return false;
        });
        binding.et4.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == 0 || actionId== EditorInfo.IME_ACTION_DONE) {
                checkAndStructurePasscode();
            }
            return false;
        });

    }

    public void checkAndStructurePasscode(){
        String a = binding.et1.getText().toString();
        String b = binding.et2.getText().toString();
        String c = binding.et3.getText().toString();
        String d = binding.et4.getText().toString();

        String code = a+b+c+d;

        if (code.length()!=4){
            Toast.makeText(this, "Please complete your passcode!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(PasscodeActivity.this,PasscodeActivity.class);

        if (TYPE_PASSCODE == PASSCODE_SETUP){
            intent.putExtra("value","retype");
            intent.putExtra("typedPasscode",code);
        }else if (TYPE_PASSCODE == PASSCODE_RETYPE){
            if (prevPass.equals(code)){
                mPref.setPasscodeForCB(code);
                Toast.makeText(this, "Passcode setup successfully!", Toast.LENGTH_SHORT).show();
                intent = new Intent(PasscodeActivity.this, UserSettingsActivity.class);
            }else {
                Toast.makeText(this, "Passcode does not match!", Toast.LENGTH_SHORT).show();
                return;
            }
        }else if (TYPE_PASSCODE == PASSCODE_CHANGE){
            if (code.equals(mPref.getPasscodeForCB())){
                intent.putExtra("value","null");
            }else {
                Toast.makeText(this, "Incorrect passcode!", Toast.LENGTH_SHORT).show();
                clearText();
                startVibration(1000);
                return;
            }
        }else {
            if (code.equals(mPref.getPasscodeForCB())){
                if (FirebaseAuth.getInstance().getUid() != null){
                    intent = getActivity();
                } else if (new UserPreferenceHelper(this).getIAmAdmin()) {
                    intent = new Intent(this, AdminMainActivity.class);
                } else {
                    intent = new Intent(this,LoginActivity.class);
                }
            }else {
                Toast.makeText(this, "Incorrect passcode!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        startVibration(200);
        startActivity(intent);

        if (TYPE_PASSCODE == PASSCODE_RETYPE){
            finishAffinity();
        }

    }

    public void startVibration(int milliseconds){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    public void clearText(){
        binding.et1.setText("");
        binding.et2.setText("");
        binding.et3.setText("");
        binding.et4.setText("");
        binding.et1.requestFocus();
    }


    Intent getActivity(){
        String s = new SharedPreference(this).getUserTypeInPref();
        switch (s){
            case "user":
                Log.e("ReactSafeLoginSystem"," user called");
                return new Intent(this, UserMainActivity.class);
            case "parent":
                if (new ParentPreferenceHelper(this).getPairedDeviceDetails() == null){
                    Log.e("ReactSafeLoginSystem","pair user called");
                    return new Intent(this, PairUserDeviceActivity.class);
                }else {
                    Log.e("ReactSafeLoginSystem","parent called");
                    return new Intent(this, ParentMainActivity.class);
                }
            case "ambulance":
                Log.e("ReactSafeLoginSystem","ambulance called");
                return new Intent(this, AmbulanceMainActivity.class);
            case "hospital":
                Log.e("ReactSafeLoginSystem","hospital called");
                return new Intent(this, HospitalMainActivity.class);
            case "police":
                Log.e("ReactSafeLoginSystem","police called");
                return new Intent(this, PoliceMainActivity.class);
            default:
                Log.e("ReactSafeLoginSystem","login called");
                return new Intent(this,LoginActivity.class);

        }
    }


}