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

import com.example.gesturaapp.ReplicationFragment;

public class AssessmentSelectionFragment extends Fragment {

    public AssessmentSelectionFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assessment_selection, container, false);

        Button btnQuiz = view.findViewById(R.id.btn_quiz);
        Button btnReplication = view.findViewById(R.id.btn_replication);

        btnQuiz.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new SubjectSelectionFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnReplication.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ReplicationSelectionFragment()); // ✅ go to selection first
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
