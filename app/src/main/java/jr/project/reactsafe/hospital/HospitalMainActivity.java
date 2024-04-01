package jr.project.reactsafe.hospital;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityHospitalMainBinding;

public class HospitalMainActivity extends AppCompatActivity {

    ActivityHospitalMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHospitalMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
}