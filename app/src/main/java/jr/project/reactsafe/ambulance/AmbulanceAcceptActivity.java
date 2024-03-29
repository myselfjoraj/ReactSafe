package jr.project.reactsafe.ambulance;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityAmbulanceAcceptBinding;

public class AmbulanceAcceptActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityAmbulanceAcceptBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_accept);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}