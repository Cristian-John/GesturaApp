package com.example.gesturaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView; // Correct import for SearchView
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LessonItemFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";

    public static LessonItemFragment newInstance(String category) {
        LessonItemFragment fragment = new LessonItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lesson_item, container, false);

        // Set up RecyclerView with GridLayout
        RecyclerView recyclerView = view.findViewById(R.id.itemRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 items per row

        // Get category data
        String category = getArguments() != null ? getArguments().getString(ARG_CATEGORY) : "";

        // Fetch items based on category
        List<LessonItem> items = LessonItemData.getItemsForCategory(category);

        // Set the adapter
        LessonItemAdapter adapter = new LessonItemAdapter(items, getContext());
        recyclerView.setAdapter(adapter);

        // Set up SearchView
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // No action on submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the adapter based on the query text
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        return view;
    }
}
