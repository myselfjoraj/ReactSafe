package jr.project.reactsafe.police;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityPoliceTransferBinding;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.NearestSafe;
import jr.project.reactsafe.extras.model.AcceptModel;
import jr.project.reactsafe.extras.model.AlertModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.CircleImageView;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.hospital.HospitalAcceptActivity;
import jr.project.reactsafe.hospital.HospitalDetailsActivity;
import jr.project.reactsafe.hospital.HospitalMainActivity;

public class PoliceTransferActivity extends AppCompatActivity {

    ActivityPoliceTransferBinding binding;

    TransferRecyclerAdapter adapter;
    ArrayList<UserModel> models;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPoliceTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.myProfImg.setOnClickListener(v -> finish());

        adapter = new TransferRecyclerAdapter();

        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setAdapter(adapter);

        NearestSafe.findPolice(model -> {
            adapter.setModel(model);
            models = model;
        });

        binding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s!=null && s.length() > 0){
                    filter(s.toString());
                }else {
                    adapter.setModel(models);
                }
            }
        });


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

    public class TransferRecyclerAdapter extends RecyclerView.Adapter<TransferRecyclerAdapter.MyViewHolder> {
        ArrayList<UserModel> models = new ArrayList<>();

        public void setModel(ArrayList<UserModel> models) {
            this.models = models;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
            return new MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            UserModel model = models.get(position);

            if (model.getProfileImage() != null)
                Glide.with(PoliceTransferActivity.this)
                        .load(model.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(holder.iv);

            holder.title.setText(model.getName());
            String d = Extras.getLocationString(PoliceTransferActivity.this,model.getLat(),model.getLng());
            holder.desc.setText(d);


            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result",new Gson().toJson(model));
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
            });


        }



        @Override
        public int getItemCount() {
            return models.size();
        }



        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc;
            LinearLayout item;
            CircleImageView iv;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                desc = itemView.findViewById(R.id.caption);
                iv = itemView.findViewById(R.id.iv);
                item = itemView.findViewById(R.id.item);
            }
        }
    }

    ProgressDialog progressDialog;
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