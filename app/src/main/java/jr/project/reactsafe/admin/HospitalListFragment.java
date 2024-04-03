package jr.project.reactsafe.admin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jr.project.reactsafe.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HospitalListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HospitalListFragment extends Fragment {

    public HospitalListFragment() {
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
        View v = inflater.inflate(R.layout.fragment_hospital_list, container, false);

        return v;
    }
}