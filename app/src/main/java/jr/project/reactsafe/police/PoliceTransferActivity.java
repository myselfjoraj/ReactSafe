package jr.project.reactsafe.police;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityPoliceTransferBinding;
import jr.project.reactsafe.extras.model.AcceptModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.CircleImageView;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.hospital.HospitalDetailsActivity;
import jr.project.reactsafe.hospital.HospitalMainActivity;

public class PoliceTransferActivity extends AppCompatActivity {

    ActivityPoliceTransferBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPoliceTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }

    public class TransferRecyclerAdapter extends RecyclerView.Adapter<TransferRecyclerAdapter.MyViewHolder> {
        ArrayList<AcceptModel> models = new ArrayList<>();

        public void setModel(ArrayList<AcceptModel> models) {
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

            UserModel model = models.get(position).getPATIENT();
            String ts = models.get(position).getTIMESTAMP();

            if (model.getProfileImage() != null)
                Glide.with(HospitalMainActivity.this)
                        .load(model.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(holder.iv);

            holder.title.setText(model.getName());
            String d = "Accident detected on " + Extras.getStandardFormDateFromTimeStamp(ts)
                    + " on " + Extras.getTimeFromTimeStamp(ts);
            holder.desc.setText(d);

            String mode = models.get(position).getSTATUS();
            String tp = models.get(position).getSTATUS();

            if (mode.equals("1")) {
                mode = "IN PROGRESS";
            } else if (mode.equals("2")) {
                mode = "CANCELLED";
            } else if (mode.equals("3")) {
                mode = "COMPLETED";
            } else {
                mode = "EXPIRED";
            }

            holder.progress.setText(mode);

            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tp.equals("1")) {
                        //mode = "IN PROGRESS";
                        startMyAct(ts, model.getUid());
                    } else if (tp.equals("2")) {
                        //mode  = "CANCELLED";
                        startMyAct(ts, null);
                    } else if (tp.equals("3")) {
                        //mode = "COMPLETED";
                        startMyAct(ts, null);
                    } else {
                        //mode = "EXPIRED";
                        startMyAct(ts, null);
                    }
                }
            });


        }


        @Override
        public int getItemCount() {
            return models.size();
        }

        void startMyAct(String id, String uid) {
            Intent intent = new Intent(HospitalMainActivity.this, HospitalDetailsActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("uid", uid);
            startActivity(intent);

        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc, progress;
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
}