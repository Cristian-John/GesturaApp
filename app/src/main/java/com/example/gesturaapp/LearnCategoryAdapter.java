package com.example.gesturaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LearnCategoryAdapter extends RecyclerView.Adapter<LearnCategoryAdapter.ViewHolder> {

    private List<LearnCategory> categoryList;
    private Context context;

    public LearnCategoryAdapter(Context context, List<LearnCategory> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleText;
        public ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            titleText = view.findViewById(R.id.category_title);
            imageView = view.findViewById(R.id.category_image);
        }
    }

    @Override
    public LearnCategoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_learn_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LearnCategoryAdapter.ViewHolder holder, int position) {
        LearnCategory category = categoryList.get(position);
        holder.titleText.setText(category.getTitle());
        holder.imageView.setImageResource(category.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.card_scale);
            v.startAnimation(anim);

            // Navigate after animation delay
            v.postDelayed(() -> {
                LessonItemFragment fragment = LessonItemFragment.newInstance(category.getTitle());

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }, 100); // delay should match your animation duration
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateList(List<LearnCategory> filteredList) {
        categoryList = filteredList;
        notifyDataSetChanged();
    }
}
