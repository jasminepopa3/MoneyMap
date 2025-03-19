package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Referințe la elementele din layout
        Button buttonAddCategory = findViewById(R.id.buttonAddCategory);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        ToolbarUtils.setupToolbar(this, toolbar);

        // Configurează RecyclerView
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(categories, this);
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Încarcă categoriile din Firestore
        loadCategories();

        // Listener pentru butonul de adăugare categorie
        buttonAddCategory.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, AddCategoryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories(); // Reîncarcă categoriile
    }

    private void loadCategories() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).collection("categories")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("Firestore", "Number of categories fetched: " + queryDocumentSnapshots.size());
                        List<Category> categories = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String id = document.getId(); // Obține ID-ul documentului
                            String name = document.getString("name");
                            String description = document.getString("description");
                            String color = document.getString("color");

                            if (name != null && color != null) {
                                categories.add(new Category(id, name, description, color)); // Folosește constructorul cu ID
                                Log.d("Firestore", "Category fetched: " + name + " with ID: " + id);
                            }
                        }
                        categoryAdapter = new CategoryAdapter(categories, this);
                        recyclerViewCategories.setAdapter(categoryAdapter);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading categories", e);
                        Toast.makeText(this, "Eroare la încărcarea categoriilor", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteCategory(Category category) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Folosește ID-ul documentului pentru ștergere
            String documentId = category.getId();
            Log.d("Firestore", "Încerc să șterg documentul cu ID: " + documentId);

            // Șterge categoria din Firestore
            db.collection("users").document(userId).collection("categories")
                    .document(documentId) // Folosește ID-ul documentului
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Categoria a fost ștearsă", Toast.LENGTH_SHORT).show();
                        loadCategories(); // Reîncarcă lista de categorii
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Eroare la ștergerea categoriei", e);
                        Toast.makeText(this, "Eroare la ștergerea categoriei", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(CategoryActivity.this, AddCategoryActivity.class);
        intent.putExtra("category", category); // Transmite categoria pentru editare
        startActivity(intent);
    }

    @Override
    public void onCategoryLongClick(Category category) {
        // Afișează un AlertDialog pentru confirmare
        new AlertDialog.Builder(this)
                .setTitle("Ștergere categorie")
                .setMessage("Ești sigur că vrei să ștergi categoria \"" + category.getName() + "\"?")
                .setPositiveButton("Da", (dialog, which) -> {
                    // Dacă utilizatorul confirmă, șterge categoria
                    deleteCategory(category);
                })
                .setNegativeButton("Nu", (dialog, which) -> {
                    // Dacă utilizatorul anulează, închide dialogul
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //meniu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (ToolbarUtils.handleOptionsItemSelected(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}