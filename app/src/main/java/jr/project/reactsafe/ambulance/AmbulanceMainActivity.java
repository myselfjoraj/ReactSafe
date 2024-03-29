package jr.project.reactsafe.ambulance;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityAmbulanceMainBinding;

public class AmbulanceMainActivity extends AppCompatActivity {

    ActivityAmbulanceMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAmbulanceMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}