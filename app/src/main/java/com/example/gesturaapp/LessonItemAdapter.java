package com.example.gesturaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LessonItemAdapter extends RecyclerView.Adapter<LessonItemAdapter.ViewHolder> implements Filterable {

    private List<LessonItem> itemList;
    private List<LessonItem> fullItemList; // full copy for filtering
    private Context context;

    public LessonItemAdapter(List<LessonItem> itemList, Context context) {
        this.itemList = itemList;
        this.fullItemList = new ArrayList<>(itemList);
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemText;

        public ViewHolder(View view) {
            super(view);
            itemText = view.findViewById(R.id.itemTitle);
        }
    }

    @Override
    public LessonItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lesson_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LessonItem item = itemList.get(position);
        holder.itemText.setText(item.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (item.getVideoFileName() != null) {
                int videoResId = context.getResources().getIdentifier(
                        item.getVideoFileName(), "raw", context.getPackageName()
                );

                String videoUri = "android.resource://" + context.getPackageName() + "/" + videoResId;

                VideoPlayerFragment fragment = VideoPlayerFragment.newInstance(item.getTitle(), videoUri);

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public Filter getFilter() {
        return lessonFilter;
    }

    private final Filter lessonFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<LessonItem> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(fullItemList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (LessonItem item : fullItemList) {
                    if (item.getTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            itemList.clear();
            //noinspection unchecked
            itemList.addAll((List<LessonItem>) results.values);
            notifyDataSetChanged();
        }
    };
}
