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

public class SubjectSelectionFragment extends Fragment {

    public SubjectSelectionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subject_selection, container, false);

        setupSubjectButton(view, R.id.btn_alphabet, "Alphabet");
        setupSubjectButton(view, R.id.btn_animals, "Animals");
        setupSubjectButton(view, R.id.btn_colors, "Colors");
        setupSubjectButton(view, R.id.btn_emotions, "Emotions");
        setupSubjectButton(view, R.id.btn_numbers, "Numbers");
        setupSubjectButton(view, R.id.btn_foods, "Food and Drinks");
        setupSubjectButton(view, R.id.btn_greetings, "Greetings");
        setupSubjectButton(view, R.id.btn_questions, "Questions");

        // Special case for Random: no subject passed (includes all questions)
        Button btnRandom = view.findViewById(R.id.btn_random);
        btnRandom.setOnClickListener(v -> {
            AssessmentFragment fragment = new AssessmentFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    private void setupSubjectButton(View view, int buttonId, String subjectName) {
        Button button = view.findViewById(buttonId);
        button.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("subject", subjectName);

            AssessmentFragment fragment = new AssessmentFragment();
            fragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
}
