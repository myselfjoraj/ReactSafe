package jr.project.reactsafe.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.reactsafe.databinding.ActivityPairParentBinding;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;

public class PairParentActivity extends AppCompatActivity {

    ActivityPairParentBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPairParentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(v -> finish());

        binding.pairBtn.setOnClickListener(v -> validatePairCode());

        binding.editTextText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s!=null && !s.toString().isEmpty()){
                    binding.editTextText.setLetterSpacing(2);
                }else {
                    binding.editTextText.setLetterSpacing(0);
                }
            }
        });


    }

    void validatePairCode(){
        String code = binding.editTextText.getText().toString();
        if (!code.isEmpty()){
            FirebaseHelper.GetPairingUser(code, model -> {
                if (model == null){
                    Toast.makeText(PairParentActivity.this, "No Pair Devices Found!", Toast.LENGTH_SHORT).show();
                    return;
                }
                model.setPairedOn(Extras.getTimestamp()+"");
                ArrayList<UserModel> userModels = new ArrayList<>();
                userModels.add(model);
                //
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid());
                dbRef.child("pairedBy").setValue(model.getUid());
                dbRef.child("pairedOn").setValue(model.getPairedOn());

                DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference().child("users").child(model.getUid());
                parentRef.child("pairedBy").setValue(FirebaseAuth.getInstance().getUid());
                parentRef.child("pairedOn").setValue(model.getPairedOn());

                //
                new UserPreferenceHelper(PairParentActivity.this)
                        .setPairedDeviceDetails(new Gson().toJson(userModels));
                startActivity(new Intent(PairParentActivity.this, PairedDevicesActivity.class));
                finish();
            });
        }else {
            Toast.makeText(this, "Please enter a pair code", Toast.LENGTH_SHORT).show();
        }

    }
}