package jr.project.reactsafe.extras.misc;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import jr.project.reactsafe.extras.model.UserModel;

public class NearestSafe {

    public static void getNearestHospital(String lati, String lngi, OnFound listener){
        double lat = Double.parseDouble(lati);
        double lng = Double.parseDouble(lngi);
        findHospital(new OnReceived() {
            @Override
            public void onReceive(ArrayList<UserModel> model) {
                if (model == null || model.isEmpty()){
                    listener.onFound(null);
                }else {
                    int i = ClosestPoint.get(lat, lng, model);
                    listener.onFound(model.get(i));
                }
            }
        });
    }

    public static void getNearestHospital(String lati, String lngi, String excludeUid,OnFound listener){
        double lat = Double.parseDouble(lati);
        double lng = Double.parseDouble(lngi);
        findHospital(new OnReceived() {
            @Override
            public void onReceive(ArrayList<UserModel> model) {
                if (model == null || model.isEmpty()){
                    listener.onFound(null);
                }else {
                    int i = ClosestPoint.get(lat, lng, model);
                    if (model.get(i).getUid().equals(excludeUid)){
                        try {
                            model.remove(i);
                            if (!model.isEmpty()) {
                                i = ClosestPoint.get(lat, lng, model);
                                listener.onFound(model.get(i));
                            }else {
                                listener.onFound(null);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {
                        listener.onFound(model.get(i));
                    }
                }
            }
        });
    }

    public static void getNearestAmbulance(String lati, String lngi, OnFound listener){
        double lat = Double.parseDouble(lati);
        double lng = Double.parseDouble(lngi);
        findAmbulance(new OnReceived() {
            @Override
            public void onReceive(ArrayList<UserModel> model) {
                if (model == null || model.isEmpty()){
                    listener.onFound(null);
                }else {
                    int i = ClosestPoint.get(lat, lng, model);
                    listener.onFound(model.get(i));
                }
            }
        });
    }

    public static void getNearestAmbulance(String lati, String lngi, String excludeUid,OnFound listener){
        double lat = Double.parseDouble(lati);
        double lng = Double.parseDouble(lngi);
        findAmbulance(new OnReceived() {
            @Override
            public void onReceive(ArrayList<UserModel> model) {
                if (model == null || model.isEmpty()){
                    listener.onFound(null);
                }else {
                    int i = ClosestPoint.get(lat, lng, model);
                    if (model.get(i).getUid().equals(excludeUid)){
                        try {
                            model.remove(i);
                            if (!model.isEmpty()) {
                                i = ClosestPoint.get(lat, lng, model);
                                listener.onFound(model.get(i));
                            }else {
                                listener.onFound(null);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {
                        listener.onFound(model.get(i));
                    }
                }
            }
        });
    }

    public static void getNearestPolice(String lati, String lngi, OnFound listener){
        double lat = Double.parseDouble(lati);
        double lng = Double.parseDouble(lngi);
        findPolice(new OnReceived() {
            @Override
            public void onReceive(ArrayList<UserModel> model) {
                if (model == null || model.isEmpty()){
                    listener.onFound(null);
                }else {
                    int i = ClosestPoint.get(lat, lng, model);
                    listener.onFound(model.get(i));
                }
            }
        });
    }


    public static void findHospital(OnReceived listener){

        FirebaseDatabase.getInstance().getReference().child("hospital")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<UserModel> m = new ArrayList<>();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                            boolean isActive = Objects.equals("true",snapshot1.child("isActive").getValue(String.class));
                            if (isActive) {
                                m.add(snapshot1.getValue(UserModel.class));
                            }
                        }
                        listener.onReceive(m);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {listener.onReceive(null);}});

    }


    public static void findAmbulance(OnReceived listener){

        FirebaseDatabase.getInstance().getReference().child("ambulance")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<UserModel> m = new ArrayList<>();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                            boolean isActive = Objects.equals("true",snapshot1.child("isActive").getValue(String.class));
                            if (isActive) {
                                m.add(snapshot1.getValue(UserModel.class));
                            }
                        }
                        listener.onReceive(m);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {listener.onReceive(null);}});

    }


    public static void findPolice(OnReceived listener){

        FirebaseDatabase.getInstance().getReference().child("police")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<UserModel> m = new ArrayList<>();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    m.add(snapshot1.getValue(UserModel.class));
                }
                listener.onReceive(m);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {listener.onReceive(null);}});

    }

    public interface OnReceived{
        void onReceive(ArrayList<UserModel> model);
    }

    public interface OnFound{
        void onFound(UserModel model);
    }

}
