package jr.project.reactsafe.ambulance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityAmbulanceMainBinding;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.model.RecentModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.CircleImageView;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.user.UserMainActivity;
import jr.project.reactsafe.user.UserPreferenceHelper;

public class AmbulanceMainActivity extends AppCompatActivity {

    ActivityAmbulanceMainBinding binding;

    String uid;
    ArrayList<UserModel> accepted,pending;
    PendingRecyclerView pendingRecyclerView;

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

        pendingRecyclerView = new PendingRecyclerView();
        binding.pendingRv.setLayoutManager(new LinearLayoutManager(this));
        binding.pendingRv.setAdapter(pendingRecyclerView);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("ambulance").child(uid);
        dbRef.child("isActive").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    boolean isActive = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    binding.start.setChecked(isActive);
                }else {
                    binding.start.setChecked(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});

        binding.start.setOnCheckedChangeListener((buttonView, isChecked) -> dbRef.child("isActive").setValue(isChecked));


        dbRef.child("alert").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        accepted.clear();
                        pending.clear();
                        for (DataSnapshot snapshot : snapshot1.getChildren()){
                            if (snapshot.exists()){
                                String isAccepted = snapshot.child("isAccepted").getValue(String.class);
                                UserModel model = snapshot.getValue(UserModel.class);
                                Log.e("AmbulanceReceived",""+new Gson().toJson(model));
                                if (model.getUid()!=null){
                                    FirebaseHelper.getUser(model.getUid(), model1 -> {
                                        pending.remove(model);
                                        model.setName(model1.getName());
                                        model.setProfileImage(model1.getProfileImage());
                                        model.setPhone(model1.getPhone());
                                        Log.e("AmbulanceReceived",""+new Gson().toJson(model1));
                                        if (isAccepted != null && isAccepted.equals("true")){
                                            accepted.add(model);
                                        }else {
                                            pending.add(model);
                                        }
                                        setNoView();
                                        pendingRecyclerView.setModel(pending);
                                    });
                                }
                                if (isAccepted != null && isAccepted.equals("true")){
                                    accepted.add(model);
                                }else {
                                    pending.add(model);
                                }
                            }
                        }
                        setNoView();
                        pendingRecyclerView.setModel(pending);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});


    }

    private void setNoView() {
        if (!pending.isEmpty()){
            binding.noItemsPendingTv.setVisibility(View.GONE);
        }else {
            binding.noItemsPendingTv.setVisibility(View.VISIBLE);
        }

        if (!accepted.isEmpty()){
            binding.noItemsAvailableTv.setVisibility(View.GONE);
        }else {
            binding.noItemsAvailableTv.setVisibility(View.VISIBLE);
        }
    }

    public class PendingRecyclerView extends RecyclerView.Adapter<PendingRecyclerView.MyViewHolder>{
        ArrayList<UserModel> models = new ArrayList<>();

        public void setModel(ArrayList<UserModel> models){
            this.models = models;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PendingRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accident_holder, parent, false);
            return new PendingRecyclerView.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull PendingRecyclerView.MyViewHolder holder, int position) {

            UserModel model = models.get(position);

            if (model.getProfileImage()!=null)
                Glide.with(AmbulanceMainActivity.this)
                        .load(model.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(holder.iv);

            holder.title.setText(model.getName());
            String d = "Accident detected on "+Extras.getStandardFormDateFromTimeStamp(model.getTimestamp())
                    +" on "+Extras.getTimeFromTimeStamp(model.getTimestamp());
            holder.desc.setText(d);

            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AmbulanceMainActivity.this,AmbulanceAcceptActivity.class);
                    intent.putExtra("id",model.getTimestamp());
                    intent.putExtra("uid",model.getUid());
                    startActivity(intent);
                }
            });

        }


        @Override
        public int getItemCount() {
            return models.size();
        }


        public class MyViewHolder extends RecyclerView.ViewHolder{
            TextView title,desc;
            CircleImageView iv;
            Button btn;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                desc  = itemView.findViewById(R.id.desc);
                btn = itemView.findViewById(R.id.button);
                iv = itemView.findViewById(R.id.pImage);
            }
        }
    }

    public class AcceptedRecyclerView extends RecyclerView.Adapter<AcceptedRecyclerView.MyViewHolder>{
        ArrayList<UserModel> models = new ArrayList<>();

        public void setModel(ArrayList<UserModel> models){
            this.models = models;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accident_history_holder, parent, false);
            return new MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            UserModel model = models.get(position);

            if (model.getProfileImage()!=null)
                Glide.with(AmbulanceMainActivity.this)
                        .load(model.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(holder.iv);

            holder.title.setText(model.getName());
            String d = "Accident detected on "+Extras.getStandardFormDateFromTimeStamp(model.getTimestamp())
                    +" on "+Extras.getTimeFromTimeStamp(model.getTimestamp());
            holder.desc.setText(d);

        }


        @Override
        public int getItemCount() {
            return models.size();
        }


        public class MyViewHolder extends RecyclerView.ViewHolder{
            TextView title,desc;
            CircleImageView iv;
            Button btn;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                desc  = itemView.findViewById(R.id.desc);
                btn = itemView.findViewById(R.id.button);
                iv = itemView.findViewById(R.id.pImage);
            }
        }
    }
}