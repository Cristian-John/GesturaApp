package com.example.gesturaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LearnFragment extends Fragment {

    RecyclerView recyclerView;
    LearnCategoryAdapter adapter;
    List<LearnCategory> categoryList;
    List<LearnCategory> filteredList;
    SearchView searchView;

    public LearnFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn, container, false);

        // Initialize RecyclerView and SearchView
        recyclerView = view.findViewById(R.id.learning_recycler_view);
        searchView = view.findViewById(R.id.search_view);

        // Set up RecyclerView with GridLayoutManager
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize the category list and the filtered list
        categoryList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Add data to categoryList (you can change the images as per your requirements)
        categoryList.add(new LearnCategory("Alphabet", R.drawable.alphabet));
        categoryList.add(new LearnCategory("Animals", R.drawable.animal));
        categoryList.add(new LearnCategory("Colors", R.drawable.color));
        categoryList.add(new LearnCategory("Emotions", R.drawable.emotions));
        categoryList.add(new LearnCategory("Foods", R.drawable.fooddrinks));
        categoryList.add(new LearnCategory("Greetings", R.drawable.greetings));
        categoryList.add(new LearnCategory("Numbers", R.drawable.numbers));
        categoryList.add(new LearnCategory("Questions", R.drawable.question));

        // Initially, the filtered list is the same as the category list
        filteredList.addAll(categoryList);

        // Set the adapter to RecyclerView
        adapter = new LearnCategoryAdapter(getContext(), filteredList);
        recyclerView.setAdapter(adapter);

        // Set up SearchView to filter categories based on query text
        setupSearch();

        return view;
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Not used, as we use onQueryTextChange for filtering
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Clear the filtered list and add matching categories based on search query
                filteredList.clear();
                for (LearnCategory item : categoryList) {
                    if (item.getTitle().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                // Update the RecyclerView adapter with the new filtered list
                adapter.updateList(filteredList);
                return true;
            }
        });
    }
}
