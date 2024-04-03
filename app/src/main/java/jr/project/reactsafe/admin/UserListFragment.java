package jr.project.reactsafe.admin;

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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.extras.model.UserModel;

public class UserListFragment extends Fragment {

    ArrayList<UserModel> models = new ArrayList<>();
    UsersListAdapter adapter;
    public UserListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_user_list, container, false);

        getUsersListFromDb();

        EditText et     = v.findViewById(R.id.search);
        RecyclerView rv = v.findViewById(R.id.rv);

        adapter = new UsersListAdapter(requireContext());

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

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

    void getUsersListFromDb(){
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    if (snapshot1.child("uid").exists() && snapshot1.child("title").exists()){
                        String title = snapshot1.child("title").getValue(String.class);
                            if (Objects.equals("user",title) || Objects.equals("parent",title)){
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