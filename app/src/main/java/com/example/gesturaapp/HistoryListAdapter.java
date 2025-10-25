package com.example.gesturaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class HistoryListAdapter extends ArrayAdapter<QuizResult> {

    public HistoryListAdapter(@NonNull Context context, ArrayList<QuizResult> results) {
        super(context, 0, results);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_history, parent, false);
        }

        QuizResult result = getItem(position);

        TextView tvType = convertView.findViewById(R.id.tvType);
        TextView tvSubject = convertView.findViewById(R.id.tvSubject);
        TextView tvScore = convertView.findViewById(R.id.tvScore);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDelete);

        // ✅ Display Type and Subject
        tvType.setText(result.getType());
        tvSubject.setText("Subject: " + result.getSubject());

        // ✅ Determine pass/fail based on type and score
        boolean passed = false;
        if (result.getType().equalsIgnoreCase("Quiz")) {
            passed = result.getScore() >= 7;
        } else if (result.getType().equalsIgnoreCase("Replication")) {
            passed = result.getScore() >= 3;
        }

        // ✅ Combine score + result in one line
        String scoreText = "Score: " + result.getScore() + "/" + result.getTotal() +
                (passed ? "  Passed" : "  Failed");
        tvScore.setText(scoreText);
        tvScore.setTextColor(passed ? 0xFF4CAF50 : 0xFFF44336); // green if passed, red if failed

        // ✅ Date
        tvDate.setText(result.getDate());

        // ✅ Delete button logic
        btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Delete Record")
                    .setMessage("Are you sure you want to delete this history?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        ZYQuizDatabaseHelper dbHelper = new ZYQuizDatabaseHelper(getContext());
                        dbHelper.deleteResult(result.getId());
                        remove(result);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return convertView;
    }
}
