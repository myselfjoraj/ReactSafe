package jr.project.reactsafe.hospital;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import jr.project.reactsafe.R;
import jr.project.reactsafe.ambulance.AmbulanceAcceptActivity;
import jr.project.reactsafe.ambulance.AmbulanceDetailsActivity;
import jr.project.reactsafe.ambulance.AmbulanceForegroundService;
import jr.project.reactsafe.ambulance.AmbulanceMainActivity;
import jr.project.reactsafe.databinding.ActivityHospitalMainBinding;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.model.AcceptModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.CircleImageView;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.user.UserPreferenceHelper;
import jr.project.reactsafe.user.UserSettingsActivity;

public class HospitalMainActivity extends AppCompatActivity {

    ActivityHospitalMainBinding binding;
    String uid;
    ArrayList<UserModel> accepted,pending;
    PendingRecyclerView pendingRecyclerView;
    AcceptedRecyclerView acceptedRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHospitalMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseHelper.getBlocked(val -> { if (val){ Extras.transferToBlocked(getApplicationContext()); }});

        uid = FirebaseAuth.getInstance().getUid();

        String name = new UserPreferenceHelper(this).getProfileName();
        String image = new UserPreferenceHelper(this).getProfileImage();
        if (image!=null)
            Glide.with(this)
                    .load(image)
                    .placeholder(R.drawable.avatar)
                    .into(binding.myProfImg);

        if (name!=null){
            binding.welcomeName.setText("Welcome "+name+",");
        }else {
            binding.welcomeName.setText("Welcome,");
        }

        binding.myProfImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HospitalMainActivity.this, UserSettingsActivity.class));
            }
        });

        accepted = new ArrayList<>();
        pending  = new ArrayList<>();

        pendingRecyclerView = new PendingRecyclerView();
        binding.pendingRv.setLayoutManager(new LinearLayoutManager(this));
        binding.pendingRv.setAdapter(pendingRecyclerView);

        acceptedRecyclerView = new AcceptedRecyclerView();
        binding.allRv.setLayoutManager(new LinearLayoutManager(this));
        binding.allRv.setAdapter(acceptedRecyclerView);


        readAllAccepts();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("hospital").child(uid);
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

        Intent intent = new Intent(HospitalMainActivity.this, HospitalForegroundService.class);
        binding.start.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startService(intent);
            }else {
                stopService(intent);
            }
            dbRef.child("isActive").setValue(isChecked);
        });


        dbRef.child("alert").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                pending.clear();
                for (DataSnapshot snapshot : snapshot1.getChildren()){
                    if (snapshot.exists()){
                        String isAccepted = snapshot.child("isAccepted").getValue(String.class);
                        UserModel model = snapshot.getValue(UserModel.class);
                        Log.e("HospitalReceived",""+new Gson().toJson(model));
                        if (model.getUid()!=null){
                            FirebaseHelper.getUser(model.getUid(), model1 -> {
                                pending.remove(model);
                                model.setName(model1.getName());
                                model.setProfileImage(model1.getProfileImage());
                                model.setPhone(model1.getPhone());
                                Log.e("HospitalReceived",""+new Gson().toJson(model1));
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
                            //accepted.add(model);
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

        if (isMyServiceRunning(HospitalForegroundService.class)){
            binding.start.setChecked(true);
            dbRef.child("isActive").setValue(true);
        }else {
            binding.start.setChecked(false);
            dbRef.child("isActive").setValue(false);
        }


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding!=null && acceptedRecyclerView!=null){
            readAllAccepts();
        }
    }

    private void readAllAccepts() {
        try (DatabaseHelper databaseHelper = new DatabaseHelper(HospitalMainActivity.this)){
            ArrayList<AcceptModel> models = databaseHelper.readHospitalAccepts();
            acceptedRecyclerView.setModel(models);
            ArrayList<String> str = new ArrayList<>();
            if (pending!=null){
                for (int i = 0 ; i < pending.size() ; i++){
                    str.add(pending.get(i).getUid());
                }
            }
            try {
                for (AcceptModel m : models) {
                    if (str.contains(m.getPATIENT().getUid())) {
                        for (int i = 0; i < pending.size(); i++) {
                            if (pending.get(i).getUid().equals(m.getPATIENT().getUid())) {
                                pending.remove(i);
                                pendingRecyclerView.setModel(pending);
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            if (!models.isEmpty()){
                binding.noItemsAvailableTv.setVisibility(View.GONE);
            }
        }
    }

    private void setNoView() {
        if (!pending.isEmpty()){
            binding.noItemsPendingTv.setVisibility(View.GONE);
        }else {
            binding.noItemsPendingTv.setVisibility(View.VISIBLE);
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
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accident_holder, parent, false);
            return new MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            UserModel model = models.get(position);

            if (model.getProfileImage()!=null)
                try{
                Glide.with(HospitalMainActivity.this)
                        .load(model.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(holder.iv);
                }catch (Exception e){
                    e.printStackTrace();
                }

            holder.title.setText(model.getName());
            String d = "Accident detected on "+ Extras.getStandardFormDateFromTimeStamp(model.getTimestamp())
                    +" on "+Extras.getTimeFromTimeStamp(model.getTimestamp());
            holder.desc.setText(d);

            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HospitalMainActivity.this, HospitalAcceptActivity.class);
                    intent.putExtra("id",model.getTimestamp());
                    intent.putExtra("uid",model.getUid());
                    startActivity(intent);
                }
            });

            holder.iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HospitalMainActivity.this, HospitalAcceptActivity.class);
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
        ArrayList<AcceptModel> models = new ArrayList<>();

        public void setModel(ArrayList<AcceptModel> models){
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

            UserModel model = models.get(position).getPATIENT();
            String ts = models.get(position).getTIMESTAMP();

            if (model.getProfileImage()!=null)
                try{
                Glide.with(HospitalMainActivity.this)
                        .load(model.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(holder.iv);
                }catch (Exception e){
                    e.printStackTrace();
                }

            holder.title.setText(model.getName());
            String d = "Accident detected on "+Extras.getStandardFormDateFromTimeStamp(ts)
                    +" on "+Extras.getTimeFromTimeStamp(ts);
            holder.desc.setText(d);

            String mode = models.get(position).getSTATUS();
            String tp = models.get(position).getSTATUS();

            if (mode.equals("1")){
                mode = "IN PROGRESS";
            }else if (mode.equals("2")){
                mode  = "CANCELLED";
            } else if (mode.equals("3")){
                mode = "COMPLETED";
            }else {
                mode = "EXPIRED";
            }

            holder.progress.setText(mode);

            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if (tp.equals("1")){
                        //mode = "IN PROGRESS";
                        startMyAct(ts,model.getUid());
                    }else if (tp.equals("2")){
                        //mode  = "CANCELLED";
                        startMyAct(ts,null);
                    } else if (tp.equals("3")){
                        //mode = "COMPLETED";
                        startMyAct(ts,null);
                    }else {
                        //mode = "EXPIRED";
                        startMyAct(ts,null);
                    }
                }
            });



        }


        @Override
        public int getItemCount() {
            return models.size();
        }

        void startMyAct(String id,String uid){
            Intent intent = new Intent(HospitalMainActivity.this, HospitalDetailsActivity.class);
            intent.putExtra("id",id);
            intent.putExtra("uid",uid);
            startActivity(intent);
        }


        public class MyViewHolder extends RecyclerView.ViewHolder{
            TextView title,desc,progress;
            LinearLayout item;
            CircleImageView iv;
            Button btn;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                desc  = itemView.findViewById(R.id.desc);
                progress = itemView.findViewById(R.id.progress);
                btn = itemView.findViewById(R.id.button);
                iv = itemView.findViewById(R.id.pImage);
                item = itemView.findViewById(R.id.item);
            }
        }
    }
}