package jr.project.reactsafe.police;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jr.project.reactsafe.R;
import jr.project.reactsafe.ambulance.AmbulanceAcceptActivity;
import jr.project.reactsafe.ambulance.AmbulanceDetailsActivity;
import jr.project.reactsafe.databinding.ActivityPoliceAcceptBinding;
import jr.project.reactsafe.extras.database.DatabaseHelper;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.DirectionsJSONParser;
import jr.project.reactsafe.extras.misc.NearestSafe;
import jr.project.reactsafe.extras.misc.SharedPreference;
import jr.project.reactsafe.extras.model.AlertModel;
import jr.project.reactsafe.extras.model.LocationModel;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;
import jr.project.reactsafe.hospital.HospitalAcceptActivity;
import jr.project.reactsafe.hospital.HospitalDetailsActivity;
import jr.project.reactsafe.parent.ParentAccidentProceedings;

public class PoliceAcceptActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityPoliceAcceptBinding binding;
    GoogleMap mMap;
    LatLng cLoc = new LatLng(37.0902, 95.7129);
    DatabaseReference dbRef;
    boolean didAccept = false;
    int sec = 60;
    AlertModel alertModel;
    ProgressDialog progressDialog;
    String id;
    UserModel patientModel,parentModel,ambulanceModel,hospitalModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPoliceAcceptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.myProfImg.setOnClickListener(v -> finish());

        id = getIntent().getStringExtra("id");
        String uid = getIntent().getStringExtra("uid");

        patientModel  = new UserModel();
        parentModel   = new UserModel();
        hospitalModel = new UserModel();
        ambulanceModel   = new UserModel();

        dbRef = FirebaseDatabase.getInstance().getReference();

        binding.time.setText("Accident Detected On "+Extras.getTimeFromTimeStamp(id));

        getPatient(uid);

        //new SharedPreference(this).putLong("startedAlertOn", Calendar.getInstance().getTimeInMillis() + 60000);

        long defaultSec = Extras.getTimestamp() + 60000;
        sec = (int) ((new SharedPreference(this)
                .getLong("startedAlertOn",defaultSec)/1000) - (Extras.getTimestamp()/1000));


        dbRef.child("users").child(uid).child("alerts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()){
                            finish();
                        }else {
                            alertModel = snapshot.getValue(AlertModel.class);
                            alertModel.setUid(uid);
                            getAmbulance(alertModel.getAmbulance());
                            getHospital(alertModel.getHospital(),alertModel.getLat(),alertModel.getLng());
                            binding.patientAddress.setText(alertModel.getLat()+" Lat, "+alertModel.getLng()+" Lng");

//                            binding.reject.setOnClickListener(v -> {
//                                changeAmbulance(false,alertModel.getLat(), alertModel.getLng(), alertModel.getTimestamp(), uid, alertModel);
//                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}});

        binding.accept.setOnClickListener(v -> {
            didAccept = true;
            dbRef.child("police").child(FirebaseAuth.getInstance().getUid()).child("alert")
                    .child(patientModel.getUid()).removeValue();
            try (DatabaseHelper db = new DatabaseHelper(PoliceAcceptActivity.this)){
                db.insertPoliceAccepts(id,alertModel.getLat(),alertModel.getLng(),"1",
                        patientModel,parentModel,hospitalModel,ambulanceModel);
                Intent intent = new Intent(PoliceAcceptActivity.this, PoliceDetailsActivity.class);
                intent.putExtra("id",alertModel.getTimestamp());
                intent.putExtra("uid",patientModel.getUid());
                startActivity(intent);
                finish();
            }

        });

        binding.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartForResult.launch(new Intent(PoliceAcceptActivity.this,PoliceTransferActivity.class));
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);



    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    UserModel model = new Gson().fromJson(intent.getStringExtra("result"),UserModel.class);

                    new LocationModel(
                            alertModel.getLat(),
                            alertModel.getLng(),
                            alertModel.getUid(),
                            alertModel.getTimestamp()
                    );

                    DatabaseReference ref =  dbRef.child("police").child(model.getUid());
                    //ref.child("alert").child(model.getTimestamp()+"").setValue(model);
                    ref.child("alert").child(patientModel.getUid()+"").setValue(model);
                    dbRef.child("users").child(patientModel.getUid()).child("alerts").child("police").setValue(model.getUid());
                   // FirebaseHelper.InsertAlertPolice(alertModel.getUid(),alertModel.getTimestamp(),model.getUid());

                    dbRef.child("police").child(FirebaseAuth.getInstance().getUid()).child("alert")
                            .child(patientModel.getUid()).removeValue();

                    try (DatabaseHelper db = new DatabaseHelper(PoliceAcceptActivity.this)){
                        db.insertPoliceAccepts(alertModel.getTimestamp(),alertModel.getLat(),alertModel.getLng(),"4",
                                patientModel,parentModel,hospitalModel,ambulanceModel);
                        finish();
                    }
                }
            });


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.addMarker(new MarkerOptions()
                .position(cLoc)
                .title("You")
                .icon(Extras.bitmapFromVector(getApplicationContext(),R.drawable.marker_map_icon)));
        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(cLoc,15));
    }

    void getPatient(String uid){
        FirebaseHelper.getUser(uid, model -> {
            patientModel = model;
            binding.patientName.setText(model.getName());
            if (model.getProfileImage()!=null)
                try {
                    Glide.with(PoliceAcceptActivity.this)
                            .load(model.getProfileImage())
                            .placeholder(R.drawable.avatar)
                            .into(binding.patientIv);
                }catch (Exception e){
                    e.printStackTrace();
                }
            binding.patientCall.setOnClickListener(v -> callPhone(model.getPhone()));
            getParent(model.getPairedBy());
        });
    }

    void getParent(String uid){
        FirebaseHelper.getUser(uid, model -> {
            parentModel = model;
            binding.parentName.setText(model.getName());
            if (model.getProfileImage()!=null)
                try{
                Glide.with(PoliceAcceptActivity.this)
                        .load(model.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(binding.parentIv);
                }catch (Exception e){
                    e.printStackTrace();
                }
            binding.parentCall.setOnClickListener(v -> callPhone(model.getPhone()));
        });
    }

    void getHospital(String uid,String lat,String lng){
        FirebaseHelper.getEntity("hospital", uid, model -> {
            hospitalModel = model;
            binding.hospitalName.setText(model.getName());
            if (model.getProfileImage()!=null)
                try{
                Glide.with(PoliceAcceptActivity.this)
                        .load(model.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(binding.hospitalIv);
                }catch (Exception e){
                    e.printStackTrace();
                }
            binding.hospitalCall.setOnClickListener(v -> callPhone(model.getPhone()));
            String loc = Extras.getLocationString(PoliceAcceptActivity.this,model.getLat(),model.getLng());
            binding.hospitalAddress2.setText(loc);
            binding.hospitalAddress.setText(loc);
            setPolyLineInMap(lat,lng,model.getLat(),model.getLng());
        });
    }

    void getAmbulance(String uid){
        FirebaseHelper.getEntity("ambulance", uid, model -> {
            ambulanceModel = model;
            binding.ambulanceName.setText(model.getName());
            if (model.getProfileImage()!=null)
                try{
                Glide.with(PoliceAcceptActivity.this)
                        .load(model.getProfileImage())
                        .placeholder(R.drawable.avatar)
                        .into(binding.ambulanceIv);
                }catch (Exception e){
                    e.printStackTrace();
                }
            binding.ambulanceCall.setOnClickListener(v -> callPhone(model.getPhone()));
            String loc = Extras.getLocationString(PoliceAcceptActivity.this,model.getLat(),model.getLng());
            binding.ambulanceAddress.setText(loc);
        });
    }

    void callPhone(String phone){
        if (phone == null || phone.isEmpty()){
            Toast.makeText(this, "Phone Number Not Provided!", Toast.LENGTH_SHORT).show();
            return;
        }
        String number = ("tel:" + phone);
        Intent mIntent = new Intent(Intent.ACTION_CALL);
        mIntent.setData(Uri.parse(number));
        if (ContextCompat.checkSelfPermission(PoliceAcceptActivity.this,
                android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PoliceAcceptActivity.this,
                    new String[]{android.Manifest.permission.CALL_PHONE},
                    124);
        } else {
            try {
                startActivity(mIntent);
            } catch(SecurityException e) {
                e.printStackTrace();
            }
        }
    }


    private void setPolyLineInMap(String lat, String lng, String hLat, String hLng) {
        if (lat == null || lng == null)
            return;
        mMap.clear();
        LatLng myLoc = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
        LatLng toLoc = new LatLng(Double.parseDouble(hLat),Double.parseDouble(hLng));
        mMap.addMarker(new MarkerOptions()
                .position(myLoc)
                .title("Child")
                .icon(Extras.bitmapFromVector(getApplicationContext(),R.drawable.marker_map_icon)));
        mMap.addMarker(new MarkerOptions()
                .position(toLoc)
                .title("Hospital")
                .icon(Extras.bitmapFromVector(getApplicationContext(),R.drawable.marker_map_icon)));
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(myLoc,11));


        String url = getDirectionsUrl(toLoc, myLoc);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = new PolylineOptions();
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(ContextCompat.getColor(PoliceAcceptActivity.this,R.color.react_safe));
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        url = "https://maps.googleapis.com/maps/api/directions/json?"+str_origin +"&"+
                str_dest +
                "&sensor=false" +
                "&mode=driving" +
                "&key="+ ContextCompat.getString(this,R.string.google_maps_key);

        Log.e("MapUrl",url);


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

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