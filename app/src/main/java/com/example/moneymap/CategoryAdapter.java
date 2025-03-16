package com.example.moneymap;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymap.Category;
import com.example.moneymap.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories; // Use a custom Category class
    private OnCategoryClickListener listener;

    // Interfață pentru a gestiona clicurile pe categorii
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onCategoryLongClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textViewCategoryName.setText(category.getName());

        // Setează culoarea de fundal
        try {
            holder.itemView.setBackgroundColor(Color.parseColor(category.getColor()));
        } catch (IllegalArgumentException e) {
            Log.e("CategoryAdapter", "Invalid color: " + category.getColor());
            holder.itemView.setBackgroundColor(Color.LTGRAY); // Culoare implicită în caz de eroare
        }

        // Gestionează clicurile
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onCategoryLongClick(category);
            return true;
        });
    }



    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoryName = itemView.findViewById(R.id.textViewCategoryName);
        }
    }
}