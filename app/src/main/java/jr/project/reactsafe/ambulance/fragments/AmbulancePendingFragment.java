package jr.project.reactsafe.ambulance.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jr.project.reactsafe.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AmbulancePendingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AmbulancePendingFragment extends Fragment {

    public AmbulancePendingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ambulance_pending, container, false);
    }
}