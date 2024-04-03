package jr.project.reactsafe.extras.database;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import jr.project.reactsafe.extras.model.LocationModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;

public class FirebaseHelper {
    /*
    * for alert insertion
    * alert -> userId -> { uid,lat,lng,timestamp, }
    *
    * for user insertion
    * users -> userId -> { uid,name,email,lat,lng,pairedBy,pairedOn}
    *
    * set up pair code
    * users -> pairCode -> {code} -> userId
    *
    * for police insertion
    * police -> policeId -> {uid,name,email,lat,lng}
    *
    * for ambulance insertion
    * ambulance -> ambulanceId -> {uid,name,email,lat,lng}
    *
    * for hospital insertion
    * hospital -> hospitalId -> {uid,name,email,lat,lng}
    *
    * for admin insertion
    * admin -> adminId -> {uid,name,email}
    *
    * */

    static DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    static String mUid = FirebaseAuth.getInstance().getUid();
    public static void InsertAlert(String ts,String lat,String lng){

        DatabaseReference ref =  dbRef.child("alert").child(mUid);
        DatabaseReference ref2 =  dbRef.child("users").child(mUid).child("alerts").child(ts);

        ref.child("uid").setValue(mUid);
        ref.child("lat").setValue(lat);
        ref.child("lng").setValue(lng);
        ref.child("timestamp").setValue(ts);

        ref2.child("lat").setValue(lat);
        ref2.child("lng").setValue(lng);
        ref2.child("timestamp").setValue(ts);
        ref2.child("status").setValue("1");
    }
    public static void InsertAlertPolice(String ts,String police){
        DatabaseReference ref =  dbRef.child("alert").child(mUid);
        DatabaseReference ref2 =  dbRef.child("users").child(mUid).child("alerts").child(ts);
        ref.child("police").setValue(police);
        ref2.child("police").setValue(police);
    }

    public static void InsertAlertPolice(String uid,String ts,String police){
        DatabaseReference ref =  dbRef.child("alert").child(uid);
        DatabaseReference ref2 =  dbRef.child("users").child(uid).child("alerts").child(ts);
        ref.child("police").setValue(police);
        ref2.child("police").setValue(police);
    }

    public static void InsertAlertHospital(String ts,String hospital){
        DatabaseReference ref =  dbRef.child("alert").child(mUid);
        DatabaseReference ref2 =  dbRef.child("users").child(mUid).child("alerts").child(ts);
        ref.child("hospital").setValue(hospital);
        ref2.child("hospital").setValue(hospital);
    }
    public static void InsertAlertAmbulance(String ts,String ambulance){
        DatabaseReference ref =  dbRef.child("alert").child(mUid);
        DatabaseReference ref2 =  dbRef.child("users").child(mUid).child("alerts").child(ts);
        ref.child("ambulance").setValue(ambulance);
        ref2.child("ambulance").setValue(ambulance);
    }


    public static void InsertAlertOnPoliceId(String police, LocationModel model){
        DatabaseReference ref =  dbRef.child("police").child(police);
        ref.child("alert").child(model.getTimestamp()+"").setValue(model);
    }

    public static void InsertAlertOnAmbulanceId(String ambulance,LocationModel model){
        DatabaseReference ref =  dbRef.child("ambulance").child(ambulance);
        ref.child("alert").child(model.getTimestamp()+"").setValue(model);
    }

    public static void InsertAlertOnHospitalId(String hospital,LocationModel model){
        DatabaseReference ref =  dbRef.child("hospital").child(hospital);
        ref.child("alert").child(model.getTimestamp()+"").setValue(model);
    }

    public static void RemoveAlert(String uid){
        dbRef.child("alert").child(uid).removeValue();
    }

    public static void InsertUser(UserModel model){
        dbRef.child("users").child(model.getUid()).setValue(model);
    }

    public static void InsertPresence(String presence){
        dbRef.child("users").child(mUid).child("presence").setValue(presence);
    }

    public static void InsertPolice(UserModel model){
        dbRef.child("police").child(model.getUid()).setValue(model);
    }

    public static void InsertAmbulance(UserModel model){
        dbRef.child("ambulance").child(model.getUid()).setValue(model);
    }

    public static void InsertHospital(UserModel model){
        dbRef.child("hospital").child(model.getUid()).setValue(model);
    }


    public static void InsertPairCode(String code){
        dbRef.child("users").child("pairCode").child(code).setValue(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
    }

    public static void InsertLocation(String lat, String lng){
        dbRef.child("users").child(mUid).child("lat").setValue(lat);
        dbRef.child("users").child(mUid).child("lng").setValue(lng);
    }

    public static void GetPairedUserLocation(String uid, OnLocationReceived listener){
        dbRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String lat = snapshot.child("lat").getValue(String.class);
                    String lng = snapshot.child("lng").getValue(String.class);

                    listener.value(lat,lng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void GetPairingUser(String code,OnReceivedUser listener){
        dbRef.child("users").child("pairCode")
                .child(code).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String uidOfPairUser = snapshot.getValue(String.class);
                            getUser(uidOfPairUser, listener);
                        }else {
                            listener.getReceiver(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.getReceiver(null);
                    }});
    }

    public static void getPairedUser(OnReceivedUser user){
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("pairedBy")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    getUser(snapshot.getValue(String.class),user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void getPresence(String mUid,OnValueReceived listener){
        dbRef.child("users").child(mUid).child("presence").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.value(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void setPresence(String presence){
        dbRef.child("users").child(mUid).child("presence").setValue(presence);
    }

    public static void getUser(String uid,OnReceivedUser listener){
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel model = snapshot.getValue(UserModel.class);
                    listener.getReceiver(model);
                }else {
                    listener.getReceiver(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.getReceiver(null);
            }
        });
    }

    public static void getEntity(String type,String uid,OnReceivedUser listener){
        FirebaseDatabase.getInstance().getReference().child(type).child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel model = snapshot.getValue(UserModel.class);
                            listener.getReceiver(model);
                        }else {
                            listener.getReceiver(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.getReceiver(null);
                    }
                });
    }

    public static void getBlocked(OnValueReceived listener){
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getUid()).child("blocked")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            boolean isBlocked = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                            listener.value(isBlocked);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public interface OnValueReceived{
        void value(boolean val);
    }

    public interface OnLocationReceived{
        void value(String lat, String lng);
    }

    public interface OnReceivedUser{
        void getReceiver(UserModel model);
    }



}
