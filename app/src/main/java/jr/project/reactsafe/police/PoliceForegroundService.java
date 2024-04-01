package jr.project.reactsafe.police;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import jr.project.reactsafe.R;
import jr.project.reactsafe.ambulance.AmbulanceMainActivity;
import jr.project.reactsafe.extras.misc.SharedPreference;

public class PoliceForegroundService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                    113,
                    createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        }else {
            startForeground(
                    113,
                    createNotification());
        }



        FirebaseDatabase.getInstance().getReference().child("police")
                .child(FirebaseAuth.getInstance().getUid()).child("alert")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        for (DataSnapshot snapshot : snapshot1.getChildren()){
                            if (snapshot.exists()){
                                String isAccepted = snapshot.child("isAccepted").getValue(String.class);
                                Log.e("PoliceReceived","A new alert received!");

                                if (isAccepted == null || !isAccepted.equals("true")){
                                    notifyHighAlert();
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});


    }

    private void notifyHighAlert() {

        Intent contentIntent = new Intent(this, PoliceMainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "lockChannel")
                .setSmallIcon(R.drawable.avatar)
                .setContentTitle("Accident Received")
                .setContentText("Please accept a nearby accident.")
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel("lockChannel", "lockChannel", NotificationManager.IMPORTANCE_DEFAULT);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(723, builder.build());

        //MediaPlayer mp = MediaPlayer.create(this, R.raw.alarm_tone);
        //AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        // ApplicationController.setMediaPlayer(mp);
        // mp.start();

        //insertFallInDb();

        new SharedPreference(this).putLong("startedAlertOn", Calendar.getInstance().getTimeInMillis() + 60000);
    }


    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel("hospital_channel", "Hospital Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, AmbulanceMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, "hospital_channel")
                .setContentTitle("React Safe")
                .setContentText("Checking for casualties.")
                .setSmallIcon(R.drawable.avatar)
                .setContentIntent(pendingIntent)
                .build();
    }
}
