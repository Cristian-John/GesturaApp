package com.example.gesturaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AssessmentSelectionFragment extends Fragment {

    public AssessmentSelectionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assessment_selection, container, false);

        Button btnQuiz = view.findViewById(R.id.btn_quiz);
        Button btnReplication = view.findViewById(R.id.btn_replication);
        Button btnViewHistory = view.findViewById(R.id.btn_view_history); // new button

        // Navigate to Quiz Section
        btnQuiz.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new SubjectSelectionFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Navigate to Replication Section
        btnReplication.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ReplicationSelectionFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Navigate to History Section
        btnViewHistory.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HistoryFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
