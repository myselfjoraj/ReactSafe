package jr.project.reactsafe.parent;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.util.Locale;
import java.util.Objects;

import jr.project.reactsafe.ApplicationController;
import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityParentAccidentProceedingsBinding;
import jr.project.reactsafe.extras.database.FirebaseHelper;
import jr.project.reactsafe.extras.misc.DirectionsJSONParser;
import jr.project.reactsafe.extras.model.UserModel;
import jr.project.reactsafe.extras.util.Extras;

public class ParentAccidentProceedings extends AppCompatActivity implements OnMapReadyCallback {

    ActivityParentAccidentProceedingsBinding binding;
    ParentPreferenceHelper mPref;
    GoogleMap mMap;
    LatLng cLoc = new LatLng(37.0902, 95.7129);
    String lat;
    String lng;
    String status;
    String ambulance;
    String hospital;
    String police;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParentAccidentProceedingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mPref = new ParentPreferenceHelper(this);
        id = mPref.getIsOnAccident();

        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            ApplicationController.releaseMediaPlayer();
            MediaPlayer player = ApplicationController.getMediaPlayer();
            if (player.isPlaying()) {
                player.stop();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if (id == null){
            finish();
        }

        setUi(null,null);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        FirebaseDatabase.getInstance().getReference().child("users")
                .child(mPref.getPairedDeviceDetails().get(0).getUid()).child("alerts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            lat = snapshot.child("lat").getValue(String.class);
                            lng = snapshot.child("lng").getValue(String.class);
                            status = snapshot.child("status").getValue(String.class);
                            ambulance = snapshot.child("ambulance").getValue(String.class);
                            hospital = snapshot.child("hospital").getValue(String.class);
                            police = snapshot.child("police").getValue(String.class);
                            if (!Objects.equals("1",status)){
                                mPref.setIsOnAccident(null);
                                finish();
                            }
                            setUi(lat,lng);
                        }else {
                            mPref.setIsOnAccident(null);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.markSafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPref.setIsOnAccident(null);
                finish();
            }
        });
    }

    void setUi(String lat,String lng){

        if (id == null){
            finish();
        }

        binding.time.setText("Accident Detected On "+Extras.getTimeFromTimeStamp(id));
        if (lat!=null && lng!=null){
            binding.childAddress.setText(lat+" Lat, "+lng+" Lng");
        }

        if (hospital!=null){
            binding.hospitalLay.setVisibility(View.VISIBLE);
            binding.noTv.setVisibility(View.GONE);
            FirebaseHelper.getEntity("hospital", hospital, new FirebaseHelper.OnReceivedUser() {
                @Override
                public void getReceiver(UserModel model) {
                    binding.hospitalName.setText(model.getName()+"");
                    binding.hospitalAddress2.setText(getLocationString(model.getLat(),model.getLng()));
                    binding.hospitalAddress.setText(getLocationString(model.getLat(),model.getLng()));
                    setPolyLineInMap(lat,lng,model.getLat(),model.getLng());
                    binding.hospitalCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callTelephone(model.getPhone());
                        }
                    });
                    if (model.getProfileImage()!=null)
                        try{
                        Glide.with(ParentAccidentProceedings.this)
                                .load(model.getProfileImage())
                                .placeholder(R.drawable.avatar)
                                .into(binding.hospitalIv);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                }
            });
        }else {
            binding.noTv.setVisibility(View.VISIBLE);
            binding.hospitalLay.setVisibility(View.GONE);
        }

        if (ambulance!=null){
            binding.ambulanceLay.setVisibility(View.VISIBLE);
            binding.noTv2.setVisibility(View.GONE);
            FirebaseHelper.getEntity("ambulance", ambulance, new FirebaseHelper.OnReceivedUser() {
                @Override
                public void getReceiver(UserModel model) {
                    binding.ambulanceName.setText(model.getName()+"");
                    binding.ambulanceAddress.setText(getLocationString(model.getLat(),model.getLng()));
                    binding.ambulanceCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callTelephone(model.getPhone());
                        }
                    });
                    if (model.getProfileImage()!=null)
                        try{
                        Glide.with(ParentAccidentProceedings.this)
                                .load(model.getProfileImage())
                                .placeholder(R.drawable.avatar)
                                .into(binding.ambulanceIv);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                }
            });
        }else {
            binding.noTv2.setVisibility(View.VISIBLE);
            binding.ambulanceLay.setVisibility(View.GONE);
        }

        if (police!=null){
            binding.policeLay.setVisibility(View.VISIBLE);
            binding.noTv3.setVisibility(View.GONE);
            FirebaseHelper.getEntity("police", police, new FirebaseHelper.OnReceivedUser() {
                @Override
                public void getReceiver(UserModel model) {
                    binding.policeName.setText(model.getName()+"");
                    binding.policeAddress.setText(getLocationString(model.getLat(),model.getLng()));
                    binding.policeCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callTelephone(model.getPhone());
                        }
                    });
                    if (model.getProfileImage()!=null)
                        try{
                        Glide.with(ParentAccidentProceedings.this)
                                .load(model.getProfileImage())
                                .placeholder(R.drawable.avatar)
                                .into(binding.policeIv);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                }
            });
        }else {
            binding.noTv3.setVisibility(View.VISIBLE);
            binding.policeLay.setVisibility(View.GONE);
        }

    }

    void callTelephone(String phone){
        if (phone == null || phone.isEmpty()){
            Toast.makeText(this, "Phone Number Not Provided!", Toast.LENGTH_SHORT).show();
            return;
        }
        String number = ("tel:" + phone);
        Intent mIntent = new Intent(Intent.ACTION_CALL);
        mIntent.setData(Uri.parse(number));
        if (ContextCompat.checkSelfPermission(ParentAccidentProceedings.this,
                android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ParentAccidentProceedings.this,
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

    String getLocationString(String lat,String lng){
        String loc = " ";
        try {

            double lati = Double.parseDouble(lat);
            double longi = Double.parseDouble(lng);

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
                lineOptions.color(ContextCompat.getColor(ParentAccidentProceedings.this,R.color.react_safe));
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
}