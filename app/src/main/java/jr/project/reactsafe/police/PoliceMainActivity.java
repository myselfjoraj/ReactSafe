package jr.project.reactsafe.police;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jr.project.reactsafe.R;
import jr.project.reactsafe.databinding.ActivityPoliceMainBinding;

public class PoliceMainActivity extends AppCompatActivity {

    ActivityPoliceMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPoliceMainBinding.inflate(getLayoutInflater())
        setContentView(R.layout.activity_police_main);

    }
}