package jr.project.reactsafe.ambulance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityAmbulanceMainBinding;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.user.UserPreferenceHelper;

public class AmbulanceMainActivity extends AppCompatActivity {

    ActivityAmbulanceMainBinding binding;

    String uid;
    ArrayList<UserModel> accepted,pending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAmbulanceMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uid = FirebaseAuth.getInstance().getUid();

        String name = new UserPreferenceHelper(this).getProfileName();

        if (name!=null){
            binding.welcomeName.setText("Welcome "+name+",");
        }else {
            binding.welcomeName.setText("Welcome,");
        }

        accepted = new ArrayList<>();
        pending  = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("ambulance")
                .child(uid).child("alert").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        accepted.clear();
                        pending.clear();
                        for (DataSnapshot snapshot : snapshot1.getChildren()){
                            if (snapshot.exists()){
                                String isAccepted = snapshot.child("isAccepted").getValue(String.class);
                                UserModel model = snapshot.getValue(UserModel.class);
                                if (isAccepted != null && isAccepted.equals("true")){
                                    accepted.add(model);
                                }else {
                                    pending.add(model);
                                }
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}