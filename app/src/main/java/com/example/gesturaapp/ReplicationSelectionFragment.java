package com.example.gesturaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ReplicationSelectionFragment extends Fragment {

    public ReplicationSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_replication_selection, container, false);

        Button btnAlphabet = view.findViewById(R.id.btnAlphabet);
        Button btnNumbers = view.findViewById(R.id.btnNumbers);
        Button btnRandom = view.findViewById(R.id.btnRandom);

        btnAlphabet.setOnClickListener(v -> openReplication("Alphabet"));
        btnNumbers.setOnClickListener(v -> openReplication("Numbers"));
        btnRandom.setOnClickListener(v -> openReplication("Random"));

        return view;
    }

    private void openReplication(String subject) {
        Bundle bundle = new Bundle();
        bundle.putString("subject", subject);

        ReplicationFragment fragment = new ReplicationFragment();
        fragment.setArguments(bundle);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
