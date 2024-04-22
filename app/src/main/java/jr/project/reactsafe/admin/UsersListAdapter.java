package jr.project.reactsafe.admin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;

import jr.project.reactsafe.R;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.CircleImageView;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.police.PoliceTransferActivity;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.MyViewHolder> {
    ArrayList<UserModel> models = new ArrayList<>();
    OnUserClick listener;
    Context context;

    public UsersListAdapter(Context context){
        this.context = context;
    }

    public void setModel(ArrayList<UserModel> models) {
        this.models = models;
        notifyDataSetChanged();
    }

    public void setListener(OnUserClick listener){
        this.listener = listener;
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

        if (model.getProfileImage() != null) {
            Glide.with(context)
                    .load(model.getProfileImage())
                    .placeholder(R.drawable.avatar)
                    .into(holder.iv);
        }else {
            Glide.with(context)
                    .load(R.drawable.avatar)
                    .into(holder.iv);
        }

        holder.title.setText(model.getName());
        holder.desc.setText(model.getEmail());

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener!=null){
                    listener.OnModelReceived(model);
                }
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

    public interface OnUserClick{
        void OnModelReceived(UserModel model);
    }
}