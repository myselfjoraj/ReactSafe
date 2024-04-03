package jr.project.reactsafe.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

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
    UsersListAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_hospital_list, container, false);

        getHospitalListFromDb();

        EditText et     = v.findViewById(R.id.search);
        RecyclerView rv = v.findViewById(R.id.rv);

        adapter = new UsersListAdapter(requireContext());

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        adapter.setListener(model -> {
            Intent i = new Intent(requireContext(),AdminUserDetailsActivity.class);
            i.putExtra("model",new Gson().toJson(model));
            startActivity(i);
        });

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s!=null && s.length() > 0){
                    filter(s.toString());
                }else {
                    adapter.setModel(models);
                }
            }
        });

        return v;
    }

    private void filter(String text) {
        ArrayList<UserModel> filteredlist = new ArrayList<>();
        for (UserModel item : models) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(item);
            }
        }
        if (!filteredlist.isEmpty()) {
            adapter.setModel(filteredlist);
        }else {
            adapter.setModel(models);
        }
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
                adapter.setModel(models);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}