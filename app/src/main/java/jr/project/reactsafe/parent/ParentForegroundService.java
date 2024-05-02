package jr.project.reactsafe.parent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import jr.project.reactsafe.ApplicationController;
import jr.project.reactsafe.R;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.LocationModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.user.AccidentAlertActivity;
import jr.project.reactsafe.user.UserMainActivity;

public class ParentForegroundService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    int sec = 60;

    ParentPreferenceHelper mPref;
    Context context;

    boolean isTimerStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                    111,
                    createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        }else {
            startForeground(
                    111,
                    createNotification());
        }

        mPref = new ParentPreferenceHelper(this);

        context = this;

        ArrayList<UserModel> model  = mPref.getPairedDeviceDetails();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getUid()).child("alert");
        ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            LocationModel locationModel = snapshot.getValue(LocationModel.class);
                            mPref.setIsOnAccident(locationModel.getTimestamp());
                            new SharedPreference(context).putString("capturedTime", locationModel.getTimestamp());
                            new DatabaseHelper(ParentForegroundService.this).insertFall(
                                    locationModel.getTimestamp(),
                                    Extras.getLocationString(ParentForegroundService.this,
                                            locationModel.getLat(), locationModel.getLng()),
                                    locationModel.getLat(), locationModel.getLng(), "1"
                            );
                            notifyAccidentDetected();
                            ref.removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

//        FirebaseDatabase.getInstance().getReference().child("alert")
//                .child(model.get(0).getUid())
//                .addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()){
//                    String ts  = snapshot.child("timestamp").getValue(String.class);
//                    String lat = snapshot.child("lat").getValue(String.class);
//                    String lng = snapshot.child("lng").getValue(String.class);
//                    new SharedPreference(context).putString("capturedTime",ts+"");
//                    mPref.setIsOnAccident(ts);
//                    if (Objects.equals(ts,"detected")){
//                        return;
//                    }
//                    if (ts == null)
//                        return;
//                    long time = Long.parseLong(ts) + 30000;
//                    long diff = time - Extras.getTimestamp();
//                    //Toast.makeText(context, " received -- "+(diff/1000), Toast.LENGTH_SHORT).show();
//                    if (diff < 30000 && diff > 0 && !isTimerStarted){
//                        int diffInSec = (int) (diff/1000);
//                        sec = (int) diffInSec;//+30
//                        Log.e("ParentForeground","diff is "+diffInSec +" second is "+sec+" ");
//                        Log.e("ParentForeground",sec+" -- balance seconds");
//                        //timerRunnable.run();
//                        timer.start();
//                    }
////                    }else if (diff >= 30000 && diff <= 60000){
////                        timer.start();
////                    }else {
////                        if (diff > 60000 && diff <= 90000){
////                            sec = (int) (diff/1000);
////                        } else {
////                            sec = 2;
////                        }
////                        timer.start();
////                    }
//
//                    new DatabaseHelper(ParentForegroundService.this).insertFall(
//                            ts,Extras.getLocationString(ParentForegroundService.this,lat,lng),lat,lng,"1"
//                    );
//
//                    Log.e("ParentForeground",diff+" -- difference");
//                }else {
//                    ApplicationController.releaseMediaPlayer();
//                    //timerHandler.removeCallbacksAndMessages(timerRunnable);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        });



    }

//    Handler timerHandler = new Handler();
//    Runnable timerRunnable = new Runnable() {
//        @Override
//        public void run() {
//            sec --;
//
//            if (sec == 0){
//                new SharedPreference(context).putLong("startedAlertOn",Extras.getTimestamp());
//                notifyHighFall();
//                timerHandler.removeCallbacksAndMessages(timerRunnable);
//            }
//
//            timerHandler.postDelayed(this, 1000);
//        }
//    };

//    int seconds = 0;
//    CountDownTimer timer = new CountDownTimer(30000L, 1000){
//        public void onTick(long millisUntilFinished){
//            if (seconds == 0){
//                seconds = sec;
//            }else {
//                seconds --;
//            }
//            isTimerStarted = true;
//            //sec --;
//            Log.e("ParentForeground",seconds+" -- seconds left");
//        }
//        public  void onFinish(){
//            isTimerStarted = false;
//            Log.e("ParentForeground","finished alert");
//            new SharedPreference(context).putLong("startedAlertOn",Extras.getTimestamp());
//            notifyHighFall();
//        }
//    };

    private void notifyAccidentDetected() {

        Intent broadcastIntent = new Intent("jr.project.reactsafe.FALL_DETECTED");
        sendBroadcast(broadcastIntent);

        Intent contentIntent = new Intent(this, ParentMainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent fullScreenIntent = new Intent(this, ParentMainActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "lockChannel")
                .setSmallIcon(R.drawable.react_safe_logo)
                .setContentTitle("Accident Detected")
                .setContentText("Please check if your child device is okay.")
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel("lockChannel", "lockChannel", NotificationManager.IMPORTANCE_DEFAULT);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(722, builder.build());

        MediaPlayer mp = MediaPlayer.create(this, R.raw.alarm_tone);
        //AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        ApplicationController.setMediaPlayer(mp);
        mp.start();

        //insertFallInDb();

        new SharedPreference(this).putLong("startedAlertOn", Calendar.getInstance().getTimeInMillis() + 30000);
    }


    private void notifyHighFall() {

        Intent broadcastIntent = new Intent("jr.project.reactsafe.FALL_DETECTED");
        sendBroadcast(broadcastIntent);

        Intent contentIntent = new Intent(this, AccidentAlertActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent fullScreenIntent = new Intent(this, AccidentAlertActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "lockChannel")
                .setSmallIcon(R.drawable.avatar)
                .setContentTitle("Accident Detected")
                .setContentText("Please check if your child device is okay.")
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel("lockChannel", "lockChannel", NotificationManager.IMPORTANCE_DEFAULT);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(722, builder.build());

        MediaPlayer mp = MediaPlayer.create(this, R.raw.alarm_tone);
        //AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        ApplicationController.setMediaPlayer(mp);
        mp.start();

        //insertFallInDb();

        new SharedPreference(this).putLong("startedAlertOn", Calendar.getInstance().getTimeInMillis() + 30000);
    }

    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel("parent_channel", "Parent Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, ParentMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, "parent_channel")
                .setContentTitle("React Safe")
                .setContentText("Monitoring safety")
                .setSmallIcon(R.drawable.react_safe_logo)
                .setContentIntent(pendingIntent)
                .build();
    }


}


