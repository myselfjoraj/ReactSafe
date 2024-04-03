package jr.project.reactsafe.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.extras.model.UserModel;

public class HospitalListFragment extends Fragment {

    public HospitalListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    ArrayList<UserModel> models = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_hospital_list, container, false);

        EditText et     = v.findViewById(R.id.search);
        RecyclerView rv = v.findViewById(R.id.rv);

        getHospitalListFromDb();

        return v;
    }

    void getHospitalListFromDb(){
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    if (snapshot1.child("uid").exists() && snapshot1.child("title").exists()){
                        String title = snapshot1.child("title").getValue(String.class);
                        if (Objects.equals("hospital",title)){
                            UserModel model = snapshot1.getValue(UserModel.class);
                            models.add(model);
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