package jr.project.reactsafe.extras.sensor;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import jr.project.reactsafe.ApplicationController;
import jr.project.reactsafe.R;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.NearestSafe;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.LocationModel;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.extras.util.ScreenReceiver;
import jr.project.reactsafe.parent.ParentPreferenceHelper;
import jr.project.reactsafe.user.AccidentAlertActivity;
import jr.project.reactsafe.user.UserMainActivity;

public class AccidentDetectionService extends Service implements SensorEventListener {

    private static final int NOTIFICATION_ID = 123;
    private static final float FALL_THRESHOLD = 20.0f;
    private static final int MIN_SAMPLES_FOR_FALL = 5;
    private static final int RESET_COUNTER_DELAY = 2000;

    private int consecutiveHighFallCounter = 0;
    private Handler handler = new Handler();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ScreenReceiver screenReceiver;
    DatabaseHelper mDatabaseHelper;

    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    SharedPreference mPref;
    Runnable locationRunner;
    Handler locationHandler = new Handler();

    String logName = "AccidentDetectionService";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                    NOTIFICATION_ID,
                    createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        }else {
            startForeground(
                    NOTIFICATION_ID,
                    createNotification());
        }
        startForeground(NOTIFICATION_ID, createNotification());
        new SharedPreference(this).putBoolean("startedReactLooks", true);
        Log.e("ReactSafeSensors", "started react looks set to true");
        FirebaseHelper.InsertPresence("online");

        screenReceiver = new ScreenReceiver();
        mDatabaseHelper = new DatabaseHelper(this);
        mPref = new SharedPreference(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);

        locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(1000 / 2)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        locationLooper();

    }

    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel("accident_channel", "Accident Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, UserMainActivity.class);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        }
        else {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }
        return new NotificationCompat.Builder(this, "accident_channel")
                .setContentTitle("React Safe")
                .setContentText("Running for your safety")
                .setSmallIcon(R.drawable.avatar)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        new SharedPreference(this).putBoolean("startedReactLooks", false);
        Log.e("ReactSafeSensors", "started react looks set to false");
        FirebaseHelper.InsertPresence("offline");

        // Clean up resources and unregister sensors
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        if (locationHandler!=null && locationRunner!=null)
            locationHandler.removeCallbacksAndMessages(locationRunner);

        unregisterReceiver(screenReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate the magnitude of acceleration
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
            // Check if the acceleration exceeds the threshold
            if (acceleration > FALL_THRESHOLD) {
                // Increment the counter for consecutive high falls
                consecutiveHighFallCounter++;
                Log.e("ReactSafeSensors", "Started FALL THRESHOLD counts");
                // Check if enough consecutive samples exceed the threshold
                if (consecutiveHighFallCounter >= MIN_SAMPLES_FOR_FALL) {
                    // High fall detected, notify and take appropriate action

                    notifyHighFall();
                    Log.e("ReactSafeSensors", "FALL THRESHOLD exceeded");
                    // Reset the counter after a delay
                    handler.postDelayed(() -> consecutiveHighFallCounter = 0, RESET_COUNTER_DELAY);
                }
            } else {
                // Reset the counter if the current sample does not exceed the threshold
                consecutiveHighFallCounter = 0;
            }
        }
    }

    private void not() {
        new SharedPreference(this).putBoolean("startedReactLooks", true);
        Intent broadcastIntent = new Intent("jr.project.reactsafe.FALL_DETECTED");
        sendBroadcast(broadcastIntent);

        Intent i = new Intent(this, AccidentAlertActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "accident_channel")
                    .setSmallIcon(R.drawable.avatar)
                    .setContentTitle("High Fall Detected")
                    .setContentText("Please check if everything is okay.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

    }

    private void notifyHighFall() {

        Intent broadcastIntent = new Intent("jr.project.reactsafe.FALL_DETECTED");
        sendBroadcast(broadcastIntent);

        Intent contentIntent = new Intent(this, AccidentAlertActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, 0);

        Intent fullScreenIntent = new Intent(this, AccidentAlertActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "lockChannel")
                .setSmallIcon(R.drawable.avatar)
                .setContentTitle("Accident Detected")
                .setContentText("Please check if everything is okay.")
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

        insertFallInDb();

        new SharedPreference(this).putLong("startedAlertOn", Calendar.getInstance().getTimeInMillis() + 30000);
    }

    Runnable timerRunnable;
    Runnable timerRunnable2;
    int sec = 0;
    int snd = 90;
    void insertFallInDb() {
        long defaultSec = Extras.getTimestamp() + 30000;
        SharedPreference mPref = new SharedPreference(this);
        sec = (int) ((mPref.getLong("startedAlertOn", defaultSec) / 1000) - (Extras.getTimestamp() / 1000));

        ArrayList<Double> db = loc();
        String id = Extras.getTimestamp()+"";
        mDatabaseHelper.insertFall(id, getLocationString(), db.get(0)+"",db.get(1)+"","1");
        FirebaseHelper.InsertAlert(id,db.get(0)+"", db.get(1)+"");

        NearestSafe.getNearestHospital(db.get(0) + "", db.get(1) + "", model -> {
            if(model!=null && model.getUid() != null && !model.getUid().isEmpty()) {
                FirebaseHelper.InsertAlertHospital(id,model.getUid());
            }
        });

        NearestSafe.getNearestAmbulance(db.get(0) + "", db.get(1) + "", model -> {
            if(model!=null && model.getUid() != null && !model.getUid().isEmpty()) {
                FirebaseHelper.InsertAlertAmbulance(id,model.getUid());
            }
        });

        NearestSafe.getNearestPolice(db.get(0) + "", db.get(1) + "", model -> {
            if(model!=null && model.getUid() != null && !model.getUid().isEmpty()) {
                FirebaseHelper.InsertAlertPolice(id,model.getUid());
            }
        });

        Handler timerHandler2 = new Handler();
        timerRunnable2 = new Runnable() {
            @Override
            public void run() {
                snd--;
                if (snd == 0) {
                    forceInsertAlertInNodes();
                    timerHandler2.removeCallbacksAndMessages(timerRunnable2);
                }
                timerHandler2.postDelayed(this, 1000);
            }
        };
        timerHandler2.post(timerRunnable2);

    }

    void forceInsertAlertInNodes(){
        FirebaseDatabase.getInstance().getReference().child("alert")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                if (!snapshot1.exists()){
                    return;
                }
                long p = mPref.getLong("startedAlertOn", Extras.getTimestamp());
                LocationModel l = new LocationModel(loc().get(0)+"",loc().get(1)+"",
                        FirebaseAuth.getInstance().getUid(),p+"");
                String police    = snapshot1.child("police").getValue(String.class);
                String ambulance = snapshot1.child("ambulance").getValue(String.class);
                String hospital  = snapshot1.child("hospital").getValue(String.class);
                if (police!=null) {
                    FirebaseHelper.InsertAlertOnPoliceId(police, l);
                }else {
                    Log.e(logName,"null police id");
                }
                if (ambulance!=null) {
                    FirebaseHelper.InsertAlertOnAmbulanceId(ambulance, l);
                }else {
                    Log.e(logName,"null ambulance id");
                }
                if (hospital!=null) {
                    FirebaseHelper.InsertAlertOnHospitalId(hospital, l);
                }else {
                    Log.e(logName,"null hospital id");
                }
                FirebaseHelper.RemoveAlert(FirebaseAuth.getInstance().getCurrentUser().getUid());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    void locationLooper(){
        locationRunner = new Runnable() {
            @Override
            public void run() {
                loopLocationRequest();
                locationHandler.postDelayed(locationRunner, 60000);
            }
        };
        locationRunner.run();
    }

    void loopLocationRequest() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            fusedLocationClient.requestLocationUpdates(null, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        mPref.putString("lastLatitude", location.getLatitude() + "");
                        mPref.putString("lastLongitude", location.getLongitude() + "");
                        mPref.putLong("lastLocationUpdate", Extras.getTimestamp());
                        Log.e("AccidentService",new Gson().toJson(location));
                    }
                }
            }, Looper.getMainLooper());
        }
    }

    String getLocationString(){
        String loc = " ";
        try {

            double lati = Double.parseDouble(mPref.getString("lastLatitude"));
            double longi = Double.parseDouble(mPref.getString("lastLongitude"));

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(lati, longi, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            loc = city+", "+state;
        }catch (Exception e){
            e.printStackTrace();
        }

        return loc;

    }

    public ArrayList<Double> loc(){
        ArrayList<Double> loc = new ArrayList<>();
        try {

            double lati = Double.parseDouble(mPref.getString("lastLatitude"));
            double longi = Double.parseDouble(mPref.getString("lastLongitude"));

            loc.add(lati);
            loc.add(longi);
        }catch (Exception e){
            e.printStackTrace();
        }

        return loc;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}