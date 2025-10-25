package com.example.gesturaapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private ListView listView;
    private TextView tvEmpty;
    private Spinner spinnerTypeFilter, spinnerDateFilter;
    private EditText searchBar;

    private ArrayList<QuizResult> allResults;
    private HistoryListAdapter adapter;
    private ZYQuizDatabaseHelper dbHelper;

    public HistoryFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // initialize views
        listView = view.findViewById(R.id.listViewHistory);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        spinnerTypeFilter = view.findViewById(R.id.spinnerTypeFilter);
        spinnerDateFilter = view.findViewById(R.id.spinnerDateFilter);
        searchBar = view.findViewById(R.id.searchBar);

        // load results
        dbHelper = new ZYQuizDatabaseHelper(requireContext());
        allResults = dbHelper.getAllResults();

        // setup adapter with initial full list
        adapter = new HistoryListAdapter(requireContext(), new ArrayList<>(allResults));
        listView.setAdapter(adapter);

        updateEmptyView();

        // spinner listeners
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerTypeFilter.setOnItemSelectedListener(filterListener);
        spinnerDateFilter.setOnItemSelectedListener(filterListener);

        // search listener (real-time)
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void applyFilters() {
        String typeFilter = spinnerTypeFilter.getSelectedItem() != null
                ? spinnerTypeFilter.getSelectedItem().toString()
                : "All";
        String dateFilter = spinnerDateFilter.getSelectedItem() != null
                ? spinnerDateFilter.getSelectedItem().toString()
                : "All";
        String searchText = searchBar.getText() != null
                ? searchBar.getText().toString().trim().toLowerCase(Locale.getDefault())
                : "";

        ArrayList<QuizResult> filtered = new ArrayList<>();
        Calendar today = Calendar.getInstance();

        for (QuizResult result : allResults) {
            // --- TYPE filter (tolerant to label variants)
            boolean matchesType = typeFilter.equalsIgnoreCase("All")
                    || typeFilter.equalsIgnoreCase(result.getType());

            // --- DATE filter
            boolean matchesDate = true;
            if (!isAllDatesLabel(dateFilter)) {
                try {
                    java.util.Date resultDate = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                            .parse(result.getDate());
                    Calendar resultCal = Calendar.getInstance();
                    resultCal.setTime(resultDate);

                    if (dateFilter.equalsIgnoreCase("Today")) {
                        matchesDate = resultCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                                && resultCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
                    } else if (dateFilter.equalsIgnoreCase("This Month")) {
                        matchesDate = resultCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                                && resultCal.get(Calendar.MONTH) == today.get(Calendar.MONTH);
                    } else if (dateFilter.equalsIgnoreCase("This Week")) {
                        // approximate week check: same week of year
                        matchesDate = resultCal.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)
                                && resultCal.get(Calendar.YEAR) == today.get(Calendar.YEAR);
                    } else if (dateFilter.equalsIgnoreCase("Oldest First")) {
                        // keep true for filter - ordering handled elsewhere if needed
                        matchesDate = true;
                    }
                } catch (Exception ignored) {
                    // if parsing fails, keep matchesDate = true (so it doesn't hide items)
                    matchesDate = true;
                }
            }

            if (!matchesType || !matchesDate) continue;

            // --- SEARCH filter: use fields available in QuizResult
            String subject = safeLower(result.getSubject());
            String type = safeLower(result.getType());
            String date = safeLower(result.getDate());
            String scoreText = result.getScore() + "/" + result.getTotal();

            // compute pass/fail text used in UI (same logic adapter uses)
            boolean passed = false;
            if (result.getType() != null && result.getType().equalsIgnoreCase("Quiz")) {
                passed = result.getScore() >= 7;
            } else if (result.getType() != null && result.getType().equalsIgnoreCase("Replication")) {
                passed = result.getScore() >= 3;
            }
            String passText = passed ? "passed" : "failed";

            boolean matchesSearch;
            if (searchText.isEmpty()) {
                matchesSearch = true;
            } else {
                matchesSearch = subject.contains(searchText)
                        || type.contains(searchText)
                        || date.contains(searchText)
                        || scoreText.contains(searchText)
                        || passText.contains(searchText);
            }

            if (matchesSearch) {
                filtered.add(result);
            }
        }

        // update adapter list
        adapter.clear();
        adapter.addAll(filtered);
        adapter.notifyDataSetChanged();

        updateEmptyView();
    }

    private boolean isAllDatesLabel(String s) {
        if (s == null) return true;
        s = s.trim().toLowerCase(Locale.getDefault());
        return s.equals("all") || s.equals("all time") || s.equals("all dates");
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.getDefault());
    }

    private void updateEmptyView() {
        if (adapter.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }
}
